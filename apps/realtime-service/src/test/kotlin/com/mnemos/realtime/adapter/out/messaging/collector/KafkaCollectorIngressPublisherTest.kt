package com.mnemos.realtime.adapter.out.messaging.collector

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventHeaders
import com.mnemos.messaging.core.transport.EventMessage
import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorPresenceChangedV2
import com.mnemos.realtime.contracts.collector.CollectorReportQueuedV2
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class KafkaCollectorIngressPublisherTest {
    @Test
    fun `uses the ingress id for report event identity and partitioning`() {
        val transport = RecordingEventTransport()
        val jsonMapper = mock<JsonMapper>()
        val publisher = KafkaCollectorIngressPublisher(transport, jsonMapper)
        val ingressId = UUID.fromString("019c1129-ef54-7000-8000-000000000420")

        whenever(jsonMapper.writeValueAsString(any())).thenReturn("serialized")

        publisher
            .publishReport(
                ingressId = ingressId,
                collectorId =
                    CollectorCredentialId(
                        UUID.fromString("019c1129-ef54-7000-8000-000000000421"),
                    ),
                rawMessage = "{}",
                activeCollectors = 1,
                receivedAt = OCCURRED_AT,
            ).toCompletableFuture()
            .join()

        val envelope = captureEnvelope(jsonMapper)
        val payload = envelope.payload as CollectorReportQueuedV2

        assertEquals(ingressId, envelope.eventId)
        assertEquals(ingressId, envelope.aggregateId)
        assertEquals(ingressId, payload.ingressId)
        assertEquals(ingressId.toString(), transport.message.destination.key)
        assertEquals(ingressId.toString(), transport.message.headers[EventHeaders.EVENT_ID])
    }

    @Test
    fun `uses one change id while partitioning all presence on the stable aggregate`() {
        val transport = RecordingEventTransport()
        val jsonMapper = mock<JsonMapper>()
        val publisher = KafkaCollectorIngressPublisher(transport, jsonMapper)

        whenever(jsonMapper.writeValueAsString(any())).thenReturn("serialized")

        publisher
            .publishPresence(
                activeCollectors = 2,
                revision = 7,
                occurredAt = OCCURRED_AT,
            ).toCompletableFuture()
            .join()

        val envelope = captureEnvelope(jsonMapper)
        val payload = envelope.payload as CollectorPresenceChangedV2

        assertEquals(payload.changeId, envelope.eventId)
        assertEquals(CollectorIngressContract.PRESENCE_AGGREGATE_ID, envelope.aggregateId)
        assertEquals(
            CollectorIngressContract.PRESENCE_AGGREGATE_ID.toString(),
            transport.message.destination.key,
        )
    }

    private fun captureEnvelope(jsonMapper: JsonMapper): EventEnvelope<*> {
        val envelope = argumentCaptor<Any>()

        verify(jsonMapper).writeValueAsString(envelope.capture())

        return envelope.firstValue as EventEnvelope<*>
    }

    private class RecordingEventTransport : EventTransport {
        lateinit var message: EventMessage

        override fun publish(message: EventMessage): CompletionStage<Unit> {
            this.message = message
            return CompletableFuture.completedFuture(Unit)
        }
    }

    private companion object {
        val OCCURRED_AT: Instant = Instant.parse("2026-08-15T10:00:00Z")
    }
}
