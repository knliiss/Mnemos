package com.mnemos.realtime.adapter.inbound.websocket.collector.ingress

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.realtime.adapter.inbound.websocket.collector.authentication.CollectorHandshakeInterceptor
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.CollectorTransportMessageCodec
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorObservationState
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorReportQueuedMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorStateUpdatedMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorTransportErrorCode
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorTransportErrorMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorUpdateSlotMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.ratelimit.CollectorMessageRateLimiter
import com.mnemos.realtime.adapter.inbound.websocket.collector.session.CollectorSessionRegistry
import com.mnemos.realtime.application.collector.model.CollectorObservationUpdateResult
import com.mnemos.realtime.application.collector.port.out.CollectorIngressPublisher
import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.util.UUID
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeUnit

@Component
class CollectorWebSocketHandler(
    private val messageCodec: CollectorTransportMessageCodec,
    private val sessionRegistry: CollectorSessionRegistry,
    private val rateLimiter: CollectorMessageRateLimiter,
    private val inFlightMessageLimiter: CollectorInFlightMessageLimiter,
    private val ingressPublisher: CollectorIngressPublisher,
    private val properties: CollectorConnectionProperties,
    private val clock: Clock,
) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val collectorId = requireCollectorId(session)

        try {
            sessionRegistry.register(
                session = session,
                collectorId = collectorId,
            )
        } catch (exception: Exception) {
            log.error(
                "Failed to acquire collector session lease, collectorId={}",
                collectorId.id,
                exception,
            )

            session.close(CloseStatus.SERVER_ERROR)
            return
        }

        publishPresence()
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        val collectorId = requireCollectorId(session)

        if (!sessionRegistry.touchActivity(session)) {
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }

        if (message.payload.toByteArray(StandardCharsets.UTF_8).size > properties.maxMessageSize.toBytes()) {
            sendError(
                session = session,
                code = CollectorTransportErrorCode.MESSAGE_TOO_LARGE,
                message = "Collector message exceeds the configured size limit",
            )
            return
        }

        if (!rateLimiter.allow(collectorId)) {
            sendError(
                session = session,
                code = CollectorTransportErrorCode.RATE_LIMITED,
                message = "Collector message rate limit exceeded",
            )
            return
        }

        val envelope =
            try {
                messageCodec.readEnvelope(message.payload)
            } catch (_: Exception) {
                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.INVALID_MESSAGE,
                    message = "Collector message envelope is invalid",
                )
                return
            }

        when (envelope.type) {
            EVENT_REPORT_TYPE ->
                handleEventReport(
                    session = session,
                    collectorId = collectorId,
                    payload = message.payload,
                )

            CHAT_PING_REPORT_TYPE ->
                handleChatPingReport(
                    session = session,
                    collectorId = collectorId,
                    payload = message.payload,
                )

            COLLECTOR_STATE_TYPE ->
                handleCollectorState(
                    session = session,
                    collectorId = collectorId,
                    payload = message.payload,
                )

            COLLECTOR_UPDATE_READY_TYPE ->
                handleCollectorUpdateReady(
                    session = session,
                    collectorId = collectorId,
                    payload = message.payload,
                )

            else ->
                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.INVALID_MESSAGE,
                    message =
                        "Only EVENT_REPORT, CHAT_PING_REPORT, COLLECTOR_STATE " +
                            "and COLLECTOR_UPDATE_READY messages are supported",
                )
        }
    }

    private fun handleEventReport(
        session: WebSocketSession,
        collectorId: CollectorCredentialId,
        payload: String,
    ) {
        val report =
            try {
                messageCodec.readReport(payload)
            } catch (_: Exception) {
                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.INVALID_MESSAGE,
                    message = "Collector report envelope is invalid",
                )
                return
            }

        if (!renewObservation(session, collectorId)) {
            return
        }

        val receivedAt = clock.instant()

        if (!inFlightMessageLimiter.tryAcquire(collectorId)) {
            sendError(
                session = session,
                code = CollectorTransportErrorCode.TOO_MANY_IN_FLIGHT,
                message = "Collector has too many reports awaiting Kafka acknowledgement",
            )
            return
        }

        try {
            val presence = sessionRegistry.presenceSnapshot()

            ingressPublisher
                .publishReport(
                    ingressId = durableIngressId(EVENT_REPORT_TYPE, collectorId, report.messageId),
                    collectorId = collectorId,
                    rawMessage = payload,
                    activeCollectors = presence.activeCollectors,
                    receivedAt = receivedAt,
                ).toCompletableFuture()
                .orTimeout(
                    properties.kafkaTimeout.toMillis(),
                    TimeUnit.MILLISECONDS,
                ).whenComplete { _: Unit?, exception: Throwable? ->
                    inFlightMessageLimiter.release(collectorId)

                    if (exception == null) {
                        sendSafely(
                            session = session,
                            payload =
                                CollectorReportQueuedMessage(
                                    messageId = report.messageId,
                                    queuedAt = clock.instant(),
                                ),
                        )
                    } else {
                        log.warn(
                            "Failed to publish collector report, collectorId={}, messageId={}",
                            collectorId.id,
                            report.messageId,
                            unwrapCompletionException(exception),
                        )

                        sendErrorSafely(
                            session = session,
                            code = CollectorTransportErrorCode.DELIVERY_UNAVAILABLE,
                            message = "Collector report could not be queued",
                        )
                    }
                }
        } catch (exception: Exception) {
            inFlightMessageLimiter.release(collectorId)

            log.warn(
                "Failed to publish collector report, collectorId={}, messageId={}",
                collectorId.id,
                report.messageId,
                exception,
            )

            sendError(
                session = session,
                code = CollectorTransportErrorCode.DELIVERY_UNAVAILABLE,
                message = "Collector report could not be queued",
            )
        }
    }

    private fun handleChatPingReport(
        session: WebSocketSession,
        collectorId: CollectorCredentialId,
        payload: String,
    ) {
        val report =
            try {
                messageCodec.readChatPing(payload)
            } catch (_: Exception) {
                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.INVALID_MESSAGE,
                    message = "Collector chat ping report is invalid",
                )
                return
            }

        val receivedAt = clock.instant()

        if (!inFlightMessageLimiter.tryAcquire(collectorId)) {
            sendError(
                session = session,
                code = CollectorTransportErrorCode.TOO_MANY_IN_FLIGHT,
                message = "Collector has too many reports awaiting Kafka acknowledgement",
            )
            return
        }

        try {
            val presence = sessionRegistry.presenceSnapshot()

            ingressPublisher
                .publishChatPing(
                    ingressId = durableIngressId(CHAT_PING_REPORT_TYPE, collectorId, report.messageId),
                    collectorId = collectorId,
                    messageId = report.messageId,
                    observedAt = report.observedAt,
                    sender = report.sender,
                    message = report.message,
                    text = report.text,
                    mentions = report.mentions,
                    activeCollectors = presence.activeCollectors,
                    receivedAt = receivedAt,
                ).toCompletableFuture()
                .orTimeout(
                    properties.kafkaTimeout.toMillis(),
                    TimeUnit.MILLISECONDS,
                ).whenComplete { _: Unit?, exception: Throwable? ->
                    inFlightMessageLimiter.release(collectorId)

                    if (exception == null) {
                        sendSafely(
                            session = session,
                            payload =
                                CollectorReportQueuedMessage(
                                    messageId = report.messageId,
                                    queuedAt = clock.instant(),
                                ),
                        )
                    } else {
                        log.warn(
                            "Failed to publish collector chat ping, collectorId={}, messageId={}",
                            collectorId.id,
                            report.messageId,
                            unwrapCompletionException(exception),
                        )
                        sendErrorSafely(
                            session = session,
                            code = CollectorTransportErrorCode.DELIVERY_UNAVAILABLE,
                            message = "Collector chat ping could not be queued",
                        )
                    }
                }
        } catch (exception: Exception) {
            inFlightMessageLimiter.release(collectorId)
            log.warn(
                "Failed to publish collector chat ping, collectorId={}, messageId={}",
                collectorId.id,
                report.messageId,
                exception,
            )
            sendError(
                session = session,
                code = CollectorTransportErrorCode.DELIVERY_UNAVAILABLE,
                message = "Collector chat ping could not be queued",
            )
        }
    }

    private fun renewObservation(
        session: WebSocketSession,
        collectorId: CollectorCredentialId,
    ): Boolean =
        try {
            if (sessionRegistry.renewObservation(session)) {
                true
            } else {
                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.NOT_OBSERVING,
                    message = "Collector must enter OBSERVING state before reporting events",
                )

                false
            }
        } catch (exception: Exception) {
            log.warn(
                "Failed to renew collector observation, collectorId={}",
                collectorId.id,
                exception,
            )

            sendError(
                session = session,
                code = CollectorTransportErrorCode.DELIVERY_UNAVAILABLE,
                message = "Collector observation state is temporarily unavailable",
            )

            false
        }

    private fun handleCollectorState(
        session: WebSocketSession,
        collectorId: CollectorCredentialId,
        payload: String,
    ) {
        val stateMessage =
            try {
                messageCodec.readState(payload)
            } catch (_: Exception) {
                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.INVALID_MESSAGE,
                    message = "Collector state message is invalid",
                )
                return
            }

        val updateResult =
            try {
                when (stateMessage.state) {
                    CollectorObservationState.OBSERVING -> sessionRegistry.markObserving(session)
                    CollectorObservationState.PAUSED -> sessionRegistry.markPaused(session)
                }
            } catch (exception: Exception) {
                log.warn(
                    "Failed to update collector observation state, collectorId={}, state={}",
                    collectorId.id,
                    stateMessage.state,
                    exception,
                )

                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.DELIVERY_UNAVAILABLE,
                    message = "Collector observation state could not be updated",
                )
                return
            }

        if (updateResult == CollectorObservationUpdateResult.SESSION_NOT_OWNED) {
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }

        if (updateResult == CollectorObservationUpdateResult.CHANGED) {
            publishPresence()
        }

        send(
            session = session,
            payload =
                CollectorStateUpdatedMessage(
                    state = stateMessage.state,
                    updatedAt = clock.instant(),
                ),
        )
    }

    private fun handleCollectorUpdateReady(
        session: WebSocketSession,
        collectorId: CollectorCredentialId,
        payload: String,
    ) {
        val updateMessage =
            try {
                messageCodec.readUpdateReady(payload)
            } catch (_: Exception) {
                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.INVALID_MESSAGE,
                    message = "Collector update readiness message is invalid",
                )
                return
            }

        if (!COLLECTOR_VERSION_PATTERN.matches(updateMessage.version)) {
            sendError(
                session = session,
                code = CollectorTransportErrorCode.INVALID_MESSAGE,
                message = "Collector update version is invalid",
            )
            return
        }

        val decision =
            try {
                sessionRegistry.requestUpdateSlot(
                    session = session,
                    version = updateMessage.version,
                )
            } catch (exception: Exception) {
                log.warn(
                    "Failed to coordinate collector update, collectorId={}, version={}",
                    collectorId.id,
                    updateMessage.version,
                    exception,
                )

                sendError(
                    session = session,
                    code = CollectorTransportErrorCode.DELIVERY_UNAVAILABLE,
                    message = "Collector update coordination is temporarily unavailable",
                )
                return
            }

        if (decision == null) {
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }

        send(
            session = session,
            payload =
                CollectorUpdateSlotMessage(
                    granted = decision.granted,
                    retryAfterSeconds = decision.retryAfterSeconds,
                ),
        )
    }

    override fun handlePongMessage(
        session: WebSocketSession,
        message: PongMessage,
    ) {
        if (!sessionRegistry.touchActivity(session) && session.isOpen) {
            session.close(CloseStatus.POLICY_VIOLATION)
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        val collectorId = sessionRegistry.unregister(session) ?: return

        rateLimiter.remove(collectorId)
        publishPresence()
    }

    override fun handleTransportError(
        session: WebSocketSession,
        exception: Throwable,
    ) {
        log.warn(
            "Collector WebSocket transport error, sessionId={}",
            session.id,
            exception,
        )

        val collectorId = sessionRegistry.unregister(session)

        if (collectorId != null) {
            rateLimiter.remove(collectorId)
            publishPresence()
        }

        if (session.isOpen) {
            session.close(CloseStatus.SERVER_ERROR)
        }
    }

    private fun publishPresence() {
        try {
            val snapshot = sessionRegistry.presenceSnapshot()

            ingressPublisher
                .publishPresence(
                    activeCollectors = snapshot.activeCollectors,
                    revision = snapshot.revision,
                    occurredAt = clock.instant(),
                ).whenComplete { _: Unit?, exception: Throwable? ->
                    if (exception != null) {
                        log.warn(
                            "Failed to publish collector presence, activeCollectors={}",
                            snapshot.activeCollectors,
                            exception,
                        )
                    }
                }
        } catch (exception: Exception) {
            log.warn("Failed to collect active collector presence", exception)
        }
    }

    private fun requireCollectorId(session: WebSocketSession): CollectorCredentialId {
        val id =
            session.attributes[CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE] as? UUID

        return CollectorCredentialId(
            requireNotNull(id) {
                "Authenticated collector id is missing from the WebSocket session"
            },
        )
    }

    private fun sendError(
        session: WebSocketSession,
        code: CollectorTransportErrorCode,
        message: String,
    ) {
        send(
            session = session,
            payload =
                CollectorTransportErrorMessage(
                    code = code,
                    message = message,
                ),
        )
    }

    private fun sendErrorSafely(
        session: WebSocketSession,
        code: CollectorTransportErrorCode,
        message: String,
    ) {
        sendSafely(
            session = session,
            payload =
                CollectorTransportErrorMessage(
                    code = code,
                    message = message,
                ),
        )
    }

    private fun sendSafely(
        session: WebSocketSession,
        payload: Any,
    ) {
        try {
            send(
                session = session,
                payload = payload,
            )
        } catch (exception: Exception) {
            log.debug(
                "Failed to send collector transport response, sessionId={}",
                session.id,
                exception,
            )
        }
    }

    private fun unwrapCompletionException(exception: Throwable): Throwable =
        if (exception is CompletionException) {
            exception.cause ?: exception
        } else {
            exception
        }

    private fun send(
        session: WebSocketSession,
        payload: Any,
    ) {
        if (!session.isOpen) {
            return
        }

        synchronized(session) {
            if (session.isOpen) {
                session.sendMessage(
                    TextMessage(messageCodec.encode(payload)),
                )
            }
        }
    }

    private fun durableIngressId(
        messageType: String,
        collectorId: CollectorCredentialId,
        messageId: UUID,
    ): UUID =
        UuidCreator.getNameBasedSha1(
            "mnemos:collector-ingress:$messageType:${collectorId.id}:$messageId",
        )

    private companion object {
        const val CHAT_PING_REPORT_TYPE = "CHAT_PING_REPORT"
        const val COLLECTOR_STATE_TYPE = "COLLECTOR_STATE"
        const val COLLECTOR_UPDATE_READY_TYPE = "COLLECTOR_UPDATE_READY"
        const val EVENT_REPORT_TYPE = "EVENT_REPORT"

        val COLLECTOR_VERSION_PATTERN = Regex("^[0-9A-Za-z][0-9A-Za-z._+-]{0,31}$")
        val log = LoggerFactory.getLogger(CollectorWebSocketHandler::class.java)
    }
}
