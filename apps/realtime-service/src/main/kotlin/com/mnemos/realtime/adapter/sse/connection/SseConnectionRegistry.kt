package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.realtime.adapter.inbound.http.sse.RealtimeBroadcastResponse
import com.mnemos.realtime.adapter.sse.protocol.RealtimeEventResponse
import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.ConnectedRealtimeBroadcastSink
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
) : ConnectedRealtimeEventSink,
    ConnectedRealtimeBroadcastSink {
    private val connectionsByUser = ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Connection>>()
    private var totalConnections = 0

    @Synchronized
    fun register(
        userId: UUID,
        clientIp: String,
        emitter: SseEmitter,
        dropSpotlightPresentationMode: DropSpotlightPresentationMode,
    ): UUID {
        val existingUserConnections = connectionsByUser[userId]

        if ((existingUserConnections?.size ?: 0) >= properties.maxConnectionsPerUser) {
            throw RealtimeConnectionLimitExceededException(
                limit = properties.maxConnectionsPerUser,
                scope = "USER",
            )
        }

        if (totalConnections >= properties.maxConnectionsTotal) {
            throw RealtimeConnectionLimitExceededException(
                limit = properties.maxConnectionsTotal,
                scope = "INSTANCE",
            )
        }

        val connectionId = UUID.randomUUID()
        val leaseResult =
            leaseStore.acquire(
                userId = userId,
                clientIp = clientIp,
                connectionId = connectionId,
            )

        when (leaseResult) {
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

        val userConnections = connectionsByUser.computeIfAbsent(userId) { ConcurrentHashMap() }

        try {
            userConnections[connectionId] =
                Connection(
                    emitter = emitter,
                    clientIp = clientIp,
                    deduplicationLimit = properties.deduplicationLimit,
                    dropSpotlightPresentationMode = dropSpotlightPresentationMode,
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
                sendSafely(event.userId, connectionId, connection) {
                    connection.sendOrQueue(event)
                }
            }
    }

    override fun send(broadcast: RealtimeBroadcast) {
        connectionsByUser.forEach { (userId, connections) ->
            connections.forEach { (connectionId, connection) ->
                sendSafely(userId, connectionId, connection) {
                    connection.sendOrQueue(broadcast)
                }
            }
        }
    }

    fun completeReplay(
        userId: UUID,
        connectionId: UUID,
        replayEvents: List<RealtimeEvent>,
        replayBroadcasts: List<RealtimeBroadcast>,
    ) {
        val connection = connectionsByUser[userId]?.get(connectionId) ?: return

        try {
            connection.completeReplay(
                replayEvents = replayEvents,
                replayBroadcasts = replayBroadcasts,
            )
        } catch (exception: Exception) {
            telemetry.recordSseSendFailure()
            remove(userId, connectionId)
            throw exception
        }
    }

    fun updateDropSpotlightPresentationMode(
        userId: UUID,
        mode: DropSpotlightPresentationMode,
    ) {
        connectionsByUser[userId]?.values?.forEach { connection ->
            connection.updateDropSpotlightPresentationMode(mode)
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
                    remove(userId, connectionId)
                }
            }
        }
    }

    fun unregister(
        userId: UUID,
        connectionId: UUID,
    ) {
        remove(userId, connectionId)
    }

    private fun sendSafely(
        userId: UUID,
        connectionId: UUID,
        connection: Connection,
        send: () -> Unit,
    ) {
        try {
            send()
        } catch (exception: Exception) {
            telemetry.recordSseSendFailure()
            log.debug(
                "Removing failed SSE connection, userId={}, connectionId={}",
                userId,
                connectionId,
                exception,
            )
            connection.completeWithError(exception)
            remove(userId, connectionId)
        }
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
        private var dropSpotlightPresentationMode: DropSpotlightPresentationMode,
    ) {
        private val pendingEvents = mutableListOf<RealtimeEvent>()
        private val pendingBroadcasts = mutableListOf<RealtimeBroadcast>()
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
        fun sendOrQueue(broadcast: RealtimeBroadcast) {
            if (replayInProgress) {
                pendingBroadcasts += broadcast
                return
            }

            send(broadcast)
        }

        @Synchronized
        fun completeReplay(
            replayEvents: List<RealtimeEvent>,
            replayBroadcasts: List<RealtimeBroadcast>,
        ) {
            val events =
                (replayEvents + pendingEvents)
                    .distinctBy(RealtimeEvent::id)
            val broadcasts =
                (replayBroadcasts + pendingBroadcasts)
                    .distinctBy(RealtimeBroadcast::id)
                    .sortedBy(RealtimeBroadcast::sequence)

            pendingEvents.clear()
            pendingBroadcasts.clear()
            replayInProgress = false

            events.forEach(::send)
            broadcasts.forEach(::send)
        }

        @Synchronized
        fun updateDropSpotlightPresentationMode(mode: DropSpotlightPresentationMode) {
            dropSpotlightPresentationMode = mode
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

        private fun send(broadcast: RealtimeBroadcast) {
            if (broadcast.id in sentEventIds) {
                return
            }

            remember(broadcast.id)

            emitter.send(
                SseEmitter
                    .event()
                    .name("drop-spotlight")
                    .data(
                        RealtimeBroadcastResponse(
                            id = broadcast.id,
                            type = "drop-spotlight",
                            sequence = broadcast.sequence,
                            occurredAt = broadcast.occurredAt,
                            expiresAt = broadcast.expiresAt,
                            presentationMode = dropSpotlightPresentationMode,
                            payload = broadcast.payload,
                        ),
                    ),
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
