package com.mnemos.event.adapter.inbound.messaging.collector

import com.mnemos.event.adapter.inbound.collector.CollectorMessageCodec
import com.mnemos.event.adapter.inbound.collector.CollectorMessageValidator
import com.mnemos.event.adapter.inbound.collector.mapper.CollectorEventPayloadMapper.toReport
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportCommand
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportUseCase
import com.mnemos.event.domain.verification.model.CollectorId
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressType
import com.mnemos.realtime.contracts.collector.CollectorReportQueuedV2
import org.springframework.stereotype.Component

@Component
class CollectorReportQueuedHandler(
    private val messageCodec: CollectorMessageCodec,
    private val messageValidator: CollectorMessageValidator,
    private val submitEventReportUseCase: SubmitEventReportUseCase,
) : KafkaMessageHandler<CollectorReportQueuedV2> {
    override val definition =
        kafkaMessageDefinition<CollectorReportQueuedV2>(
            CollectorIngressContract.TOPIC,
            CollectorIngressType.REPORT_QUEUED,
            CollectorIngressContract.VERSION,
            CollectorIngressContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != REALTIME_SERVICE) {
                throw KafkaMessageProtocolException("Collector reports must be produced by realtime-service")
            }

            if (envelope.aggregateId != envelope.payload.ingressId) {
                throw KafkaMessageProtocolException("Collector report aggregateId must match ingressId")
            }

            if (envelope.eventId != envelope.payload.ingressId) {
                throw KafkaMessageProtocolException("Collector report eventId must match ingressId")
            }

            if (envelope.occurredAt != envelope.payload.receivedAt) {
                throw KafkaMessageProtocolException("Collector report occurredAt must match receivedAt")
            }

            if (envelope.payload.activeCollectors <= 0) {
                throw KafkaMessageProtocolException("Collector report must contain an active collector")
            }
        }

    override val consumer = "event-service.collector-report"

    override fun handle(message: ConsumedMessage<CollectorReportQueuedV2>) {
        val payload = message.payload
        val report =
            try {
                messageCodec.decodeReport(payload.rawMessage)
            } catch (exception: Exception) {
                throw KafkaMessageProtocolException(
                    message = "Collector report payload is invalid",
                    cause = exception,
                )
            }

        val violations = messageValidator.validate(report)

        if (violations.isNotEmpty()) {
            throw KafkaMessageProtocolException(
                "Collector report validation failed: ${violations.joinToString { it.field }}",
            )
        }

        submitEventReportUseCase.execute(
            SubmitEventReportCommand(
                collectorId = CollectorId(payload.collectorId.toString()),
                messageId = report.messageId,
                observedAt = report.observedAt,
                receivedAt = payload.receivedAt,
                activeCollectorCount = payload.activeCollectors,
                report = report.event.toReport(),
            ),
        )
    }

    private companion object {
        const val REALTIME_SERVICE = "realtime-service"
    }
}
