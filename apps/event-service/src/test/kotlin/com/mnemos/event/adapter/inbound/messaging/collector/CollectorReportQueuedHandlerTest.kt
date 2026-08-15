package com.mnemos.event.adapter.inbound.messaging.collector

import com.mnemos.event.adapter.inbound.collector.CollectorMessageCodec
import com.mnemos.event.adapter.inbound.collector.CollectorMessageValidator
import com.mnemos.event.adapter.inbound.collector.dto.CollectorEventReportMessage
import com.mnemos.event.adapter.inbound.collector.dto.report.GlobalCollectorEventPayload
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportCommand
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportUseCase
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.verification.model.CollectorId
import com.mnemos.event.domain.verification.model.report.GlobalCollectorEventReport
import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressType
import com.mnemos.realtime.contracts.collector.CollectorReportQueuedV2
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class CollectorReportQueuedHandlerTest {
    private val messageCodec = mock<CollectorMessageCodec>()
    private val messageValidator = mock<CollectorMessageValidator>()
    private val submitEventReportUseCase = mock<SubmitEventReportUseCase>()
    private val handler =
        CollectorReportQueuedHandler(
            messageCodec = messageCodec,
            messageValidator = messageValidator,
            submitEventReportUseCase = submitEventReportUseCase,
        )

    @Test
    fun `decodes kafka ingress and submits the collector report`() {
        val ingressId = UUID.fromString("019c1129-ef54-7000-8000-000000000070")
        val collectorId = UUID.fromString("019c1129-ef54-7000-8000-000000000071")
        val messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000072")
        val observedAt = Instant.parse("2026-08-15T10:04:00Z")
        val receivedAt = observedAt.plusMillis(25)
        val rawMessage = "{\"type\":\"EVENT_REPORT\"}"
        val report =
            CollectorEventReportMessage(
                messageId = messageId,
                observedAt = observedAt,
                event =
                    GlobalCollectorEventPayload(
                        eventType = DomainGlobalEventType.MOON,
                    ),
            )
        val payload =
            CollectorReportQueuedV2(
                ingressId = ingressId,
                collectorId = collectorId,
                rawMessage = rawMessage,
                activeCollectors = 1,
                receivedAt = receivedAt,
            )

        whenever(messageCodec.decodeReport(rawMessage)).thenReturn(report)
        whenever(messageValidator.validate(report)).thenReturn(emptyList())

        handler.handle(
            ConsumedMessage(
                messageId = ingressId,
                type = CollectorIngressType.REPORT_QUEUED,
                version = CollectorIngressContract.VERSION,
                producer = "realtime-service",
                aggregateType = CollectorIngressContract.AGGREGATE_TYPE,
                aggregateId = ingressId,
                occurredAt = receivedAt,
                metadata = EventMetadata(),
                payload = payload,
            ),
        )

        verify(submitEventReportUseCase).execute(
            eq(
                SubmitEventReportCommand(
                    collectorId = CollectorId(collectorId.toString()),
                    messageId = messageId,
                    observedAt = observedAt,
                    receivedAt = receivedAt,
                    activeCollectorCount = 1,
                    report =
                        GlobalCollectorEventReport(
                            type = DomainGlobalEventType.MOON,
                        ),
                ),
            ),
        )
    }

    @Test
    fun `rejects collector ingress from any service except realtime service`() {
        val ingressId = UUID.fromString("019c1129-ef54-7000-8000-000000000080")
        val payload =
            CollectorReportQueuedV2(
                ingressId = ingressId,
                collectorId = UUID.fromString("019c1129-ef54-7000-8000-000000000081"),
                rawMessage = "{}",
                activeCollectors = 1,
                receivedAt = Instant.parse("2026-08-15T10:05:00Z"),
            )

        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                EventEnvelope(
                    eventId = UUID.fromString("019c1129-ef54-7000-8000-000000000082"),
                    eventType = CollectorIngressType.REPORT_QUEUED,
                    eventVersion = CollectorIngressContract.VERSION,
                    producer = "event-service",
                    aggregateType = CollectorIngressContract.AGGREGATE_TYPE,
                    aggregateId = ingressId,
                    occurredAt = payload.receivedAt,
                    metadata = EventMetadata(),
                    payload = payload,
                ),
            )
        }
    }
}
