package com.mnemos.realtime.adapter.inbound.websocket.collector.ingress

import com.mnemos.realtime.adapter.inbound.websocket.collector.authentication.CollectorHandshakeInterceptor
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.CollectorTransportMessageCodec
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorChatPingEnvelope
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorMessageEnvelope
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorObservationState
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorReportEnvelope
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorReportQueuedMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorStateMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorStateUpdatedMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorTransportErrorCode
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorTransportErrorMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.ratelimit.CollectorMessageRateLimiter
import com.mnemos.realtime.adapter.inbound.websocket.collector.session.CollectorSessionRegistry
import com.mnemos.realtime.application.collector.model.CollectorObservationUpdateResult
import com.mnemos.realtime.application.collector.model.CollectorPresenceSnapshot
import com.mnemos.realtime.application.collector.port.out.CollectorIngressPublisher
import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.CompletableFuture

class CollectorWebSocketHandlerTest {
    @Test
    fun `does not block the websocket thread and acknowledges only after Kafka acknowledges`() {
        val properties = CollectorConnectionProperties()
        val clock = Clock.fixed(RECEIVED_AT, ZoneOffset.UTC)
        val messageCodec = mock<CollectorTransportMessageCodec>()
        val sessionRegistry = mock<CollectorSessionRegistry>()
        val publisher = mock<CollectorIngressPublisher>()
        val publishResult = CompletableFuture<Unit>()
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000220"),
            )
        val messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000221")
        val session = mock<WebSocketSession>()
        val handler =
            CollectorWebSocketHandler(
                messageCodec = messageCodec,
                sessionRegistry = sessionRegistry,
                rateLimiter = CollectorMessageRateLimiter(properties, clock),
                inFlightMessageLimiter = CollectorInFlightMessageLimiter(properties),
                ingressPublisher = publisher,
                properties = properties,
                clock = clock,
            )
        val payload = """{"type":"EVENT_REPORT","messageId":"$messageId"}"""

        whenever(session.attributes).thenReturn(
            mutableMapOf<String, Any>(
                CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE to collectorId.id,
            ),
        )
        whenever(session.isOpen).thenReturn(true)
        whenever(sessionRegistry.touchActivity(session)).thenReturn(true)
        whenever(sessionRegistry.renewObservation(session)).thenReturn(true)
        whenever(sessionRegistry.presenceSnapshot()).thenReturn(CollectorPresenceSnapshot(2, 4))
        whenever(messageCodec.readEnvelope(payload)).thenReturn(CollectorMessageEnvelope("EVENT_REPORT"))
        whenever(messageCodec.readReport(payload)).thenReturn(CollectorReportEnvelope("EVENT_REPORT", messageId))
        whenever(messageCodec.encode(any())).thenReturn("queued")
        whenever(
            publisher.publishReport(
                ingressId = any(),
                collectorId = any(),
                rawMessage = any(),
                activeCollectors = any(),
                receivedAt = any(),
            ),
        ).thenReturn(publishResult)

        handler.handleMessage(session, TextMessage(payload))

        verify(session, never()).sendMessage(any())

        publishResult.complete(Unit)

        val sentMessage = argumentCaptor<TextMessage>()
        verify(session).sendMessage(sentMessage.capture())
        assertEquals("queued", sentMessage.firstValue.payload)
    }

    @Test
    fun `chat ping waits for Kafka and acknowledges the original message id`() {
        val properties = CollectorConnectionProperties()
        val clock = Clock.fixed(RECEIVED_AT, ZoneOffset.UTC)
        val messageCodec = mock<CollectorTransportMessageCodec>()
        val sessionRegistry = mock<CollectorSessionRegistry>()
        val publisher = mock<CollectorIngressPublisher>()
        val publishResult = CompletableFuture<Unit>()
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000226"),
            )
        val messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000227")
        val observedAt = Instant.parse("2026-10-03T19:00:00Z")
        val session = mock<WebSocketSession>()
        val handler =
            CollectorWebSocketHandler(
                messageCodec = messageCodec,
                sessionRegistry = sessionRegistry,
                rateLimiter = CollectorMessageRateLimiter(properties, clock),
                inFlightMessageLimiter = CollectorInFlightMessageLimiter(properties),
                ingressPublisher = publisher,
                properties = properties,
                clock = clock,
            )
        val payload = """{"type":"CHAT_PING_REPORT","messageId":"$messageId"}"""
        val ping =
            CollectorChatPingEnvelope(
                type = "CHAT_PING_REPORT",
                messageId = messageId,
                observedAt = observedAt,
                sender = "TranZorXx",
                message = "MVP ┃ TranZorXx [#401] » @Knaliz hello",
                text = "@Knaliz hello",
                mentions = listOf("Knaliz"),
            )

        whenever(session.attributes).thenReturn(
            mutableMapOf<String, Any>(
                CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE to collectorId.id,
            ),
        )
        whenever(session.isOpen).thenReturn(true)
        whenever(sessionRegistry.touchActivity(session)).thenReturn(true)
        whenever(sessionRegistry.presenceSnapshot()).thenReturn(CollectorPresenceSnapshot(0, 4))
        whenever(messageCodec.readEnvelope(payload)).thenReturn(CollectorMessageEnvelope("CHAT_PING_REPORT"))
        whenever(messageCodec.readChatPing(payload)).thenReturn(ping)
        whenever(messageCodec.encode(any())).thenReturn("queued")
        whenever(
            publisher.publishChatPing(
                ingressId = any(),
                collectorId = any(),
                messageId = any(),
                observedAt = any(),
                sender = any(),
                message = any(),
                text = any(),
                mentions = any(),
                activeCollectors = any(),
                receivedAt = any(),
            ),
        ).thenReturn(publishResult)

        handler.handleMessage(session, TextMessage(payload))

        verify(session, never()).sendMessage(any())

        publishResult.complete(Unit)

        verify(publisher).publishChatPing(
            ingressId = any(),
            collectorId = eq(collectorId),
            messageId = eq(messageId),
            observedAt = eq(observedAt),
            sender = eq("TranZorXx"),
            message = eq("MVP ┃ TranZorXx [#401] » @Knaliz hello"),
            text = eq("@Knaliz hello"),
            mentions = eq(listOf("Knaliz")),
            activeCollectors = eq(0),
            receivedAt = eq(RECEIVED_AT),
        )
        verify(sessionRegistry, never()).renewObservation(session)

        val response = argumentCaptor<Any>()
        verify(messageCodec).encode(response.capture())

        val queued = response.firstValue as CollectorReportQueuedMessage
        assertEquals(messageId, queued.messageId)
    }

    @Test
    fun `does not queue a report while the collector is paused`() {
        val properties = CollectorConnectionProperties()
        val clock = Clock.fixed(RECEIVED_AT, ZoneOffset.UTC)
        val messageCodec = mock<CollectorTransportMessageCodec>()
        val sessionRegistry = mock<CollectorSessionRegistry>()
        val publisher = mock<CollectorIngressPublisher>()
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000222"),
            )
        val messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000223")
        val session = mock<WebSocketSession>()
        val handler =
            CollectorWebSocketHandler(
                messageCodec = messageCodec,
                sessionRegistry = sessionRegistry,
                rateLimiter = CollectorMessageRateLimiter(properties, clock),
                inFlightMessageLimiter = CollectorInFlightMessageLimiter(properties),
                ingressPublisher = publisher,
                properties = properties,
                clock = clock,
            )
        val payload = """{"type":"EVENT_REPORT","messageId":"$messageId"}"""

        whenever(session.attributes).thenReturn(
            mutableMapOf<String, Any>(
                CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE to collectorId.id,
            ),
        )
        whenever(session.isOpen).thenReturn(true)
        whenever(sessionRegistry.touchActivity(session)).thenReturn(true)
        whenever(sessionRegistry.renewObservation(session)).thenReturn(false)
        whenever(messageCodec.readEnvelope(payload)).thenReturn(CollectorMessageEnvelope("EVENT_REPORT"))
        whenever(messageCodec.readReport(payload)).thenReturn(CollectorReportEnvelope("EVENT_REPORT", messageId))
        whenever(messageCodec.encode(any())).thenReturn("not-observing")

        handler.handleMessage(session, TextMessage(payload))

        verify(publisher, never()).publishReport(
            ingressId = any(),
            collectorId = any(),
            rawMessage = any(),
            activeCollectors = any(),
            receivedAt = any(),
        )

        val response = argumentCaptor<Any>()
        verify(messageCodec).encode(response.capture())

        val error = response.firstValue as CollectorTransportErrorMessage
        assertEquals(CollectorTransportErrorCode.NOT_OBSERVING, error.code)
    }

    @Test
    fun `activates observation and publishes presence without reconnecting`() {
        val properties = CollectorConnectionProperties()
        val clock = Clock.fixed(RECEIVED_AT, ZoneOffset.UTC)
        val messageCodec = mock<CollectorTransportMessageCodec>()
        val sessionRegistry = mock<CollectorSessionRegistry>()
        val publisher = mock<CollectorIngressPublisher>()
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000224"),
            )
        val session = mock<WebSocketSession>()
        val handler =
            CollectorWebSocketHandler(
                messageCodec = messageCodec,
                sessionRegistry = sessionRegistry,
                rateLimiter = CollectorMessageRateLimiter(properties, clock),
                inFlightMessageLimiter = CollectorInFlightMessageLimiter(properties),
                ingressPublisher = publisher,
                properties = properties,
                clock = clock,
            )
        val payload = """{"type":"COLLECTOR_STATE","state":"OBSERVING"}"""

        whenever(session.attributes).thenReturn(
            mutableMapOf<String, Any>(
                CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE to collectorId.id,
            ),
        )
        whenever(session.isOpen).thenReturn(true)
        whenever(sessionRegistry.touchActivity(session)).thenReturn(true)
        whenever(sessionRegistry.markObserving(session)).thenReturn(CollectorObservationUpdateResult.CHANGED)
        whenever(sessionRegistry.presenceSnapshot()).thenReturn(CollectorPresenceSnapshot(1, 5))
        whenever(messageCodec.readEnvelope(payload)).thenReturn(CollectorMessageEnvelope("COLLECTOR_STATE"))
        whenever(messageCodec.readState(payload)).thenReturn(
            CollectorStateMessage(
                type = "COLLECTOR_STATE",
                state = CollectorObservationState.OBSERVING,
            ),
        )
        whenever(messageCodec.encode(any())).thenReturn("state-updated")
        whenever(
            publisher.publishPresence(
                activeCollectors = 1,
                revision = 5,
                occurredAt = RECEIVED_AT,
            ),
        ).thenReturn(CompletableFuture.completedFuture(Unit))

        handler.handleMessage(session, TextMessage(payload))

        verify(sessionRegistry).markObserving(session)
        verify(publisher).publishPresence(
            activeCollectors = 1,
            revision = 5,
            occurredAt = RECEIVED_AT,
        )

        val response = argumentCaptor<Any>()
        verify(messageCodec).encode(response.capture())

        val stateUpdated = response.firstValue as CollectorStateUpdatedMessage
        assertEquals(CollectorObservationState.OBSERVING, stateUpdated.state)
        assertEquals(RECEIVED_AT, stateUpdated.updatedAt)
    }

    @Test
    fun `pauses observation without closing the websocket`() {
        val properties = CollectorConnectionProperties()
        val clock = Clock.fixed(RECEIVED_AT, ZoneOffset.UTC)
        val messageCodec = mock<CollectorTransportMessageCodec>()
        val sessionRegistry = mock<CollectorSessionRegistry>()
        val publisher = mock<CollectorIngressPublisher>()
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000225"),
            )
        val session = mock<WebSocketSession>()
        val handler =
            CollectorWebSocketHandler(
                messageCodec = messageCodec,
                sessionRegistry = sessionRegistry,
                rateLimiter = CollectorMessageRateLimiter(properties, clock),
                inFlightMessageLimiter = CollectorInFlightMessageLimiter(properties),
                ingressPublisher = publisher,
                properties = properties,
                clock = clock,
            )
        val payload = """{"type":"COLLECTOR_STATE","state":"PAUSED"}"""

        whenever(session.attributes).thenReturn(
            mutableMapOf<String, Any>(
                CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE to collectorId.id,
            ),
        )
        whenever(session.isOpen).thenReturn(true)
        whenever(sessionRegistry.touchActivity(session)).thenReturn(true)
        whenever(sessionRegistry.markPaused(session)).thenReturn(CollectorObservationUpdateResult.CHANGED)
        whenever(sessionRegistry.presenceSnapshot()).thenReturn(CollectorPresenceSnapshot(0, 6))
        whenever(messageCodec.readEnvelope(payload)).thenReturn(CollectorMessageEnvelope("COLLECTOR_STATE"))
        whenever(messageCodec.readState(payload)).thenReturn(
            CollectorStateMessage(
                type = "COLLECTOR_STATE",
                state = CollectorObservationState.PAUSED,
            ),
        )
        whenever(messageCodec.encode(any())).thenReturn("state-updated")
        whenever(
            publisher.publishPresence(
                activeCollectors = 0,
                revision = 6,
                occurredAt = RECEIVED_AT,
            ),
        ).thenReturn(CompletableFuture.completedFuture(Unit))

        handler.handleMessage(session, TextMessage(payload))

        verify(sessionRegistry).markPaused(session)
        verify(session, never()).close(any())
        verify(publisher).publishPresence(
            activeCollectors = 0,
            revision = 6,
            occurredAt = RECEIVED_AT,
        )

        val response = argumentCaptor<Any>()
        verify(messageCodec).encode(response.capture())

        val stateUpdated = response.firstValue as CollectorStateUpdatedMessage
        assertEquals(CollectorObservationState.PAUSED, stateUpdated.state)
    }

    private companion object {
        val RECEIVED_AT: Instant = Instant.parse("2026-08-15T10:00:00Z")
    }
}
