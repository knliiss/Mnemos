package com.mnemos.notification.adapter.inbound.messaging.collector

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.notification.application.ping.ProcessPingNotificationCommand
import com.mnemos.notification.application.ping.ProcessPingNotificationService
import com.mnemos.realtime.contracts.collector.CollectorChatPingQueuedV2
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressType
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID

class CollectorChatPingQueuedHandlerTest {
    private val service = mock<ProcessPingNotificationService>()
    private val handler = CollectorChatPingQueuedHandler(service)

    @Test
    fun `maps collector ping ingress to notification processing command`() {
        val ingressId = UUID.fromString("019c1180-0000-7000-8000-000000000010")
        val messageId = UUID.fromString("019c1180-0000-7000-8000-000000000011")
        val observedAt = Instant.parse("2026-10-03T19:00:00Z")
        val receivedAt = observedAt.plusMillis(25)
        val payload =
            CollectorChatPingQueuedV2(
                ingressId = ingressId,
                collectorId = UUID.fromString("019c1180-0000-7000-8000-000000000012"),
                messageId = messageId,
                observedAt = observedAt,
                sender = "TranZorXx",
                message = "MVP ┃ TranZorXx [#401] » @Knaliz hello",
                text = "@Knaliz hello",
                mentions = listOf("Knaliz"),
                activeCollectors = 2,
                receivedAt = receivedAt,
            )

        handler.handle(
            ConsumedMessage(
                messageId = ingressId,
                type = CollectorIngressType.CHAT_PING_QUEUED,
                version = CollectorIngressContract.VERSION,
                producer = "realtime-service",
                aggregateType = CollectorIngressContract.AGGREGATE_TYPE,
                aggregateId = ingressId,
                occurredAt = receivedAt,
                metadata = EventMetadata(),
                payload = payload,
            ),
        )

        verify(service).process(
            eq(
                ProcessPingNotificationCommand(
                    messageId = messageId,
                    observedAt = observedAt,
                    sender = "TranZorXx",
                    message = "MVP ┃ TranZorXx [#401] » @Knaliz hello",
                    text = "@Knaliz hello",
                    mentions = listOf("Knaliz"),
                ),
            ),
        )
    }

    @Test
    fun `rejects collector chat ping not produced by realtime service`() {
        val ingressId = UUID.fromString("019c1180-0000-7000-8000-000000000020")
        val receivedAt = Instant.parse("2026-10-03T19:00:00Z")
        val payload =
            CollectorChatPingQueuedV2(
                ingressId = ingressId,
                collectorId = UUID.fromString("019c1180-0000-7000-8000-000000000021"),
                messageId = UUID.fromString("019c1180-0000-7000-8000-000000000022"),
                observedAt = receivedAt,
                sender = "TranZorXx",
                message = "TranZorXx [#401] » @Knaliz hello",
                text = "@Knaliz hello",
                mentions = listOf("Knaliz"),
                activeCollectors = 1,
                receivedAt = receivedAt,
            )

        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                EventEnvelope(
                    eventId = ingressId,
                    eventType = CollectorIngressType.CHAT_PING_QUEUED,
                    eventVersion = CollectorIngressContract.VERSION,
                    producer = "event-service",
                    aggregateType = CollectorIngressContract.AGGREGATE_TYPE,
                    aggregateId = ingressId,
                    occurredAt = receivedAt,
                    metadata = EventMetadata(),
                    payload = payload,
                ),
            )
        }
    }
}
