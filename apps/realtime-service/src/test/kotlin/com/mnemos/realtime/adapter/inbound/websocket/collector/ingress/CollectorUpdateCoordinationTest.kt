package com.mnemos.realtime.adapter.inbound.websocket.collector.ingress

import com.mnemos.realtime.adapter.inbound.websocket.collector.authentication.CollectorHandshakeInterceptor
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.CollectorTransportMessageCodec
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorMessageEnvelope
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorUpdateReadyMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorUpdateSlotMessage
import com.mnemos.realtime.adapter.inbound.websocket.collector.ratelimit.CollectorMessageRateLimiter
import com.mnemos.realtime.adapter.inbound.websocket.collector.session.CollectorSessionRegistry
import com.mnemos.realtime.application.collector.model.CollectorUpdateSlotDecision
import com.mnemos.realtime.application.collector.port.out.CollectorIngressPublisher
import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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

class CollectorUpdateCoordinationTest {
    @Test
    fun `returns update slot decision without pausing before client acknowledgement`() {
        val properties = CollectorConnectionProperties()
        val clock = Clock.fixed(NOW, ZoneOffset.UTC)
        val messageCodec = mock<CollectorTransportMessageCodec>()
        val sessionRegistry = mock<CollectorSessionRegistry>()
        val publisher = mock<CollectorIngressPublisher>()
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000230"),
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
        val payload = """{"type":"COLLECTOR_UPDATE_READY","version":"0.2.0"}"""

        whenever(session.attributes).thenReturn(
            mutableMapOf<String, Any>(
                CollectorHandshakeInterceptor.COLLECTOR_ID_ATTRIBUTE to collectorId.id,
            ),
        )
        whenever(session.isOpen).thenReturn(true)
        whenever(sessionRegistry.touchActivity(session)).thenReturn(true)
        whenever(messageCodec.readEnvelope(payload)).thenReturn(
            CollectorMessageEnvelope("COLLECTOR_UPDATE_READY"),
        )
        whenever(messageCodec.readUpdateReady(payload)).thenReturn(
            CollectorUpdateReadyMessage(
                type = "COLLECTOR_UPDATE_READY",
                version = "0.2.0",
            ),
        )
        whenever(
            sessionRegistry.requestUpdateSlot(
                session = session,
                version = "0.2.0",
            ),
        ).thenReturn(
            CollectorUpdateSlotDecision(
                granted = false,
                retryAfterSeconds = 30,
            ),
        )
        whenever(messageCodec.encode(any())).thenReturn("update-slot")

        handler.handleMessage(session, TextMessage(payload))

        verify(sessionRegistry).requestUpdateSlot(
            session = session,
            version = "0.2.0",
        )
        verify(sessionRegistry, never()).markPaused(session)

        val response = argumentCaptor<Any>()
        verify(messageCodec).encode(response.capture())

        val updateSlot = response.firstValue as CollectorUpdateSlotMessage
        assertEquals(false, updateSlot.granted)
        assertEquals(30L, updateSlot.retryAfterSeconds)
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-23T19:00:00Z")
    }
}
