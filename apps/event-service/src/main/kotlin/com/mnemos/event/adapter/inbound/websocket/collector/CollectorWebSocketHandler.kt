package com.mnemos.event.adapter.inbound.websocket.collector

import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorClientMessage
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorErrorCode
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorErrorCode.VALIDATION_FAILED
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorErrorMessage
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorEventReportMessage
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorFieldViolation
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorHelloAckMessage
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorHelloMessage
import com.mnemos.event.adapter.inbound.websocket.collector.mapper.CollectorEventPayloadMapper.toReport
import com.mnemos.event.adapter.inbound.websocket.collector.mapper.CollectorEventReportMapper.toAck
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportCommand
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportUseCase
import com.mnemos.event.domain.verification.model.CollectorId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.core.JacksonException
import tools.jackson.core.exc.StreamReadException
import tools.jackson.databind.DatabindException
import java.time.Instant

@Component
class CollectorWebSocketHandler(
    private val messageCodec: CollectorMessageCodec,
    private val messageValidator: CollectorMessageValidator,
    private val sessionRegistry: CollectorSessionRegistry,
    private val submitEventReportUseCase: SubmitEventReportUseCase,
) : TextWebSocketHandler() {
    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        sessionRegistry.touch(session)
        val clientMessage =
            decodeMessage(
                session = session,
                payload = message.payload,
            ) ?: return

        val violations = messageValidator.validate(clientMessage)

        if (violations.isNotEmpty()) {
            sendError(
                session = session,
                code = VALIDATION_FAILED,
                message = "Message validation failed",
                violations = violations,
            )
            return
        }

        try {
            when (clientMessage) {
                is CollectorHelloMessage ->
                    handleHello(
                        session = session,
                        message = clientMessage,
                    )

                is CollectorEventReportMessage ->
                    handleEventReport(
                        session = session,
                        message = clientMessage,
                    )
            }
        } catch (exception: Exception) {
            log.error(
                "Unexpected collector WebSocket error, sessionId={}",
                session.id,
                exception,
            )

            sendError(
                session = session,
                code = CollectorErrorCode.INTERNAL_ERROR,
                message = "Internal server error",
            )
        }
    }

    override fun handlePongMessage(
        session: WebSocketSession,
        message: PongMessage,
    ) {
        sessionRegistry.touch(session)
    }

    private fun decodeMessage(
        session: WebSocketSession,
        payload: String,
    ): CollectorClientMessage? =
        try {
            messageCodec.decode(payload)
        } catch (_: StreamReadException) {
            sendError(
                session = session,
                code = CollectorErrorCode.MALFORMED_JSON,
                message = "Malformed JSON",
            )
            null
        } catch (_: DatabindException) {
            sendError(
                session = session,
                code = CollectorErrorCode.INVALID_MESSAGE,
                message = "Invalid message",
            )
            null
        } catch (_: JacksonException) {
            sendError(
                session = session,
                code = CollectorErrorCode.INVALID_MESSAGE,
                message = "Invalid message",
            )
            null
        }

    private fun handleHello(
        session: WebSocketSession,
        message: CollectorHelloMessage,
    ) {
        if (sessionRegistry.findCollectorId(session) != null) {
            sendError(
                session = session,
                code = CollectorErrorCode.PROTOCOL_VIOLATION,
                message = "Collector is already registered",
            )
            return
        }

        if (message.protocolVersion != PROTOCOL_VERSION) {
            sendError(
                session = session,
                code = CollectorErrorCode.PROTOCOL_VIOLATION,
                message = "Unsupported protocol version",
            )

            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }

        val collectorId = CollectorId(message.collectorId)

        sessionRegistry.register(
            session = session,
            collectorId = collectorId,
        )

        send(
            session = session,
            payload =
                CollectorHelloAckMessage(
                    protocolVersion = PROTOCOL_VERSION,
                    collectorId = collectorId.id,
                    activeCollectors = sessionRegistry.activeCollectorCount(),
                ),
        )
    }

    private fun handleEventReport(
        session: WebSocketSession,
        message: CollectorEventReportMessage,
    ) {
        val collectorId = sessionRegistry.findCollectorId(session)

        if (collectorId == null) {
            sendError(
                session = session,
                code = CollectorErrorCode.PROTOCOL_VIOLATION,
                message = "HELLO must be sent before EVENT_REPORT!",
            )
            return
        }

        val result =
            submitEventReportUseCase.execute(
                SubmitEventReportCommand(
                    collectorId = collectorId,
                    messageId = message.messageId,
                    observedAt = message.observedAt,
                    receivedAt = Instant.now(),
                    report = message.event.toReport(),
                ),
            )
        send(
            session = session,
            payload = result.toAck(message.messageId),
        )
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        sessionRegistry.unregister(session)
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

        sessionRegistry.unregister(session)

        if (session.isOpen) {
            session.close(CloseStatus.SERVER_ERROR)
        }
    }

    private fun sendError(
        session: WebSocketSession,
        code: CollectorErrorCode,
        message: String,
        violations: List<CollectorFieldViolation> = emptyList(),
    ) {
        send(
            session = session,
            payload =
                CollectorErrorMessage(
                    code = code,
                    message = message,
                    violations = violations,
                ),
        )
    }

    private fun send(
        session: WebSocketSession,
        payload: Any,
    ) {
        if (!session.isOpen) {
            return
        }

        session.sendMessage(
            TextMessage(
                messageCodec.encode(payload),
            ),
        )
    }

    init {
        log.info("Init")
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(CollectorWebSocketHandler::class.java)

        private const val PROTOCOL_VERSION = 1
    }
}
