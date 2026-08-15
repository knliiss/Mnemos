package com.mnemos.event.adapter.inbound.messaging.collector

import com.mnemos.event.application.collector.port.out.CollectorCoverageRecorder
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressType
import com.mnemos.realtime.contracts.collector.CollectorPresenceChangedV2
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID

class CollectorPresenceChangedHandlerTest {
    @Test
    fun `records a versioned collector presence snapshot`() {
        val recorder = mock<CollectorCoverageRecorder>()
        val handler = CollectorPresenceChangedHandler(recorder)
        val occurredAt = Instant.parse("2026-08-15T10:00:00Z")
        val changeId = UUID.fromString("019c1129-ef54-7000-8000-000000000430")
        val payload =
            CollectorPresenceChangedV2(
                changeId = changeId,
                activeCollectors = 3,
                revision = 12,
                occurredAt = occurredAt,
            )

        handler.handle(
            ConsumedMessage(
                messageId = changeId,
                type = CollectorIngressType.PRESENCE_CHANGED,
                version = CollectorIngressContract.VERSION,
                producer = "realtime-service",
                aggregateType = CollectorIngressContract.AGGREGATE_TYPE,
                aggregateId = CollectorIngressContract.PRESENCE_AGGREGATE_ID,
                occurredAt = occurredAt,
                metadata = EventMetadata(),
                payload = payload,
            ),
        )

        verify(recorder).record(
            activeCollectors = 3,
            occurredAt = occurredAt,
            revision = 12,
        )
    }
}
