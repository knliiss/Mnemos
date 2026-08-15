package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.realtime.adapter.sse.protocol.RealtimeEventResponse
import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.port.out.ConnectedRealtimeEventSink
import com.mnemos.realtime.application.delivery.port.out.RealtimeDeliveryTelemetry
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class SseConnectionRegistry(
    private val properties: RealtimeDeliveryProperties,
    private val telemetry: RealtimeDeliveryTelemetry,
    private val leaseStore: SseConnectionLeaseStore,
) : ConnectedRealtimeEventSink {
    private val connectionsByUser = ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Connection>>()
    private var totalConnections = 0

    @Synchronized
    fun register(
        userId: UUID,
        clientIp: String,
        emitter: SseEmitter,
    ): UUID {
        val userConnections =
            connectionsByUser.computeIfAbsent(userId) { ConcurrentHashMap() }

        if (userConnections.size >= properties.maxConnectionsPerUser) {
            removeEmptyUserConnections(userId, userConnections)

            throw RealtimeConnectionLimitExceededException(
                limit = properties.maxConnectionsPerUser,
                scope = "USER",
            )
        }

        if (totalConnections >= properties.maxConnectionsTotal) {
            removeEmptyUserConnections(userId, userConnections)

            throw RealtimeConnectionLimitExceededException(
                limit = properties.maxConnectionsTotal,
                scope = "INSTANCE",
            )
        }

        val connectionId = UUID.randomUUID()

        when (
            leaseStore.acquire(
                userId = userId,
                clientIp = clientIp,
                connectionId = connectionId,
            )
        ) {
            SseConnectionLeaseAcquireResult.ACQUIRED -> Unit
            SseConnectionLeaseAcquireResult.USER_LIMIT_EXCEEDED ->
                throw RealtimeConnectionLimitExceededException(
                    limit = properties.maxConnectionsPerUser,
                    scope = "CLUSTER_USER",
                )

            SseConnectionLeaseAcquireResult.CLIENT_IP_LIMIT_EXCEEDED ->
                throw RealtimeConnectionLimitExceededException(
                    limit = properties.maxConnectionsPerClientIp,
                    scope = "CLUSTER_CLIENT_IP",
                )

            SseConnectionLeaseAcquireResult.TOTAL_LIMIT_EXCEEDED ->
                throw RealtimeConnectionLimitExceededException(
                    limit = properties.maxConnectionsTotal,
                    scope = "CLUSTER",
                )
        }

        try {
            userConnections[connectionId] =
                Connection(
                    emitter = emitter,
                    clientIp = clientIp,
                    deduplicationLimit = properties.deduplicationLimit,
                )
            totalConnections += 1
            telemetry.connectionOpened()
        } catch (exception: Exception) {
            releaseLease(userId, clientIp, connectionId)
            removeEmptyUserConnections(userId, userConnections)
            throw exception
        }

        emitter.onCompletion {
            remove(
                userId = userId,
                connectionId = connectionId,
            )
        }
        emitter.onTimeout {
            remove(
                userId = userId,
                connectionId = connectionId,
            )
        }
        emitter.onError {
            remove(
                userId = userId,
                connectionId = connectionId,
            )
        }

        return connectionId
    }

    override fun send(event: RealtimeEvent) {
        connectionsByUser[event.userId]
            ?.forEach { (connectionId, connection) ->
                try {
                    connection.sendOrQueue(event)
                } catch (exception: Exception) {
                    telemetry.recordSseSendFailure()
                    log.debug(
                        "Removing failed SSE connection, userId={}, connectionId={}",
                        event.userId,
                        connectionId,
                        exception,
                    )

                    connection.completeWithError(exception)
                    remove(
                        userId = event.userId,
                        connectionId = connectionId,
                    )
                }
            }
    }

    fun completeReplay(
        userId: UUID,
        connectionId: UUID,
        replayEvents: List<RealtimeEvent>,
    ) {
        val connection = connectionsByUser[userId]?.get(connectionId) ?: return

        try {
            connection.completeReplay(replayEvents)
        } catch (exception: Exception) {
            telemetry.recordSseSendFailure()
            log.debug(
                "Removing failed SSE connection during replay, userId={}, connectionId={}",
                userId,
                connectionId,
                exception,
            )

            remove(
                userId = userId,
                connectionId = connectionId,
            )

            throw exception
        }
    }

    fun heartbeat() {
        connectionsByUser.forEach { (userId, connections) ->
            connections.forEach { (connectionId, connection) ->
                try {
                    val renewed =
                        leaseStore.renew(
                            userId = userId,
                            clientIp = connection.clientIp,
                            connectionId = connectionId,
                        )

                    check(renewed) {
                        "SSE connection lease expired"
                    }

                    connection.heartbeat()
                } catch (exception: Exception) {
                    telemetry.recordSseSendFailure()
                    connection.completeWithError(exception)
                    remove(
                        userId = userId,
                        connectionId = connectionId,
                    )
                }
            }
        }
    }

    fun unregister(
        userId: UUID,
        connectionId: UUID,
    ) {
        remove(
            userId = userId,
            connectionId = connectionId,
        )
    }

    @Synchronized
    private fun remove(
        userId: UUID,
        connectionId: UUID,
    ) {
        val connections = connectionsByUser[userId] ?: return
        val connection = connections.remove(connectionId) ?: return

        totalConnections -= 1
        telemetry.connectionClosed()
        releaseLease(
            userId = userId,
            clientIp = connection.clientIp,
            connectionId = connectionId,
        )

        if (connections.isEmpty()) {
            connectionsByUser.remove(userId, connections)
        }
    }

    private fun releaseLease(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    ) {
        try {
            leaseStore.release(
                userId = userId,
                clientIp = clientIp,
                connectionId = connectionId,
            )
        } catch (exception: Exception) {
            log.warn(
                "Failed to release SSE connection lease, userId={}, connectionId={}",
                userId,
                connectionId,
                exception,
            )
        }
    }

    private fun removeEmptyUserConnections(
        userId: UUID,
        connections: ConcurrentHashMap<UUID, Connection>,
    ) {
        if (connections.isEmpty()) {
            connectionsByUser.remove(userId, connections)
        }
    }

    private class Connection(
        val emitter: SseEmitter,
        val clientIp: String,
        private val deduplicationLimit: Int,
    ) {
        private val pendingEvents = mutableListOf<RealtimeEvent>()
        private val sentEventIds = mutableSetOf<UUID>()
        private val sentEventOrder = ArrayDeque<UUID>()
        private var replayInProgress = true

        @Synchronized
        fun sendOrQueue(event: RealtimeEvent) {
            if (replayInProgress) {
                pendingEvents += event
                return
            }

            send(event)
        }

        @Synchronized
        fun completeReplay(replayEvents: List<RealtimeEvent>) {
            val events =
                (replayEvents + pendingEvents)
                    .distinctBy(RealtimeEvent::id)

            pendingEvents.clear()
            replayInProgress = false

            events.forEach(::send)
        }

        @Synchronized
        fun heartbeat() {
            emitter.send(
                SseEmitter
                    .event()
                    .comment("heartbeat"),
            )
        }

        fun completeWithError(exception: Exception) {
            try {
                emitter.completeWithError(exception)
            } catch (_: Exception) {
                // The connection is removed by the registry regardless of emitter state.
            }
        }

        private fun send(event: RealtimeEvent) {
            if (event.id in sentEventIds) {
                return
            }

            remember(event.id)

            emitter.send(
                SseEmitter
                    .event()
                    .id(event.id.toString())
                    .name(event.type.eventName)
                    .data(event.toResponse()),
            )
        }

        private fun RealtimeEvent.toResponse(): RealtimeEventResponse =
            RealtimeEventResponse(
                id = id,
                type = type.eventName,
                occurredAt = occurredAt,
                operationId = operationId,
                payload = payload,
            )

        private fun remember(eventId: UUID) {
            sentEventIds += eventId
            sentEventOrder += eventId

            while (sentEventOrder.size > deduplicationLimit) {
                sentEventIds -= sentEventOrder.removeFirst()
            }
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(SseConnectionRegistry::class.java)
    }
}
