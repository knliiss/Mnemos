package com.mnemos.realtime.adapter.out.messaging.collector

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventHeaders
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.core.transport.EventMessage
import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto.CollectorChatPingEnvelope
import com.mnemos.realtime.application.collector.port.out.CollectorIngressPublisher
import com.mnemos.realtime.contracts.collector.CollectorChatPingQueuedV2
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressType
import com.mnemos.realtime.contracts.collector.CollectorPresenceChangedV2
import com.mnemos.realtime.contracts.collector.CollectorReportQueuedV2
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletionStage

@Component
class KafkaCollectorIngressPublisher(
    private val transport: EventTransport,
    private val jsonMapper: JsonMapper,
) : CollectorIngressPublisher {
    override fun publishReport(
        ingressId: UUID,
        collectorId: CollectorCredentialId,
        rawMessage: String,
        activeCollectors: Int,
        receivedAt: Instant,
    ): CompletionStage<Unit> =
        publish(
            eventId = ingressId,
            type = CollectorIngressType.REPORT_QUEUED,
            aggregateId = ingressId,
            occurredAt = receivedAt,
            payload =
                CollectorReportQueuedV2(
                    ingressId = ingressId,
                    collectorId = collectorId.id,
                    rawMessage = rawMessage,
                    activeCollectors = activeCollectors,
                    receivedAt = receivedAt,
                ),
        )

    override fun publishChatPing(
        ingressId: UUID,
        collectorId: CollectorCredentialId,
        report: CollectorChatPingEnvelope,
        activeCollectors: Int,
        receivedAt: Instant,
    ): CompletionStage<Unit> =
        publish(
            eventId = ingressId,
            type = CollectorIngressType.CHAT_PING_QUEUED,
            aggregateId = ingressId,
            occurredAt = receivedAt,
            payload =
                CollectorChatPingQueuedV2(
                    ingressId = ingressId,
                    collectorId = collectorId.id,
                    messageId = report.messageId,
                    observedAt = report.observedAt,
                    sender = report.sender,
                    message = report.message,
                    text = report.text,
                    mentions = report.mentions,
                    activeCollectors = activeCollectors,
                    receivedAt = receivedAt,
                ),
        )

    override fun publishPresence(
        activeCollectors: Int,
        revision: Long,
        occurredAt: Instant,
    ): CompletionStage<Unit> {
        val changeId = UuidCreator.getTimeOrderedEpoch()

        return publish(
            eventId = changeId,
            type = CollectorIngressType.PRESENCE_CHANGED,
            aggregateId = CollectorIngressContract.PRESENCE_AGGREGATE_ID,
            occurredAt = occurredAt,
            payload =
                CollectorPresenceChangedV2(
                    changeId = changeId,
                    activeCollectors = activeCollectors,
                    revision = revision,
                    occurredAt = occurredAt,
                ),
        )
    }

    private fun publish(
        eventId: UUID,
        type: String,
        aggregateId: UUID,
        occurredAt: Instant,
        payload: Any,
    ): CompletionStage<Unit> {
        val envelope =
            EventEnvelope(
                eventId = eventId,
                eventType = type,
                eventVersion = CollectorIngressContract.VERSION,
                producer = PRODUCER,
                aggregateType = CollectorIngressContract.AGGREGATE_TYPE,
                aggregateId = aggregateId,
                occurredAt = occurredAt,
                metadata = EventMetadata(),
                payload = payload,
            )

        return transport.publish(
            EventMessage(
                destination =
                    EventDestination(
                        topic = CollectorIngressContract.TOPIC,
                        key = aggregateId.toString(),
                    ),
                payload = jsonMapper.writeValueAsString(envelope),
                headers =
                    mapOf(
                        EventHeaders.EVENT_ID to eventId.toString(),
                        EventHeaders.EVENT_TYPE to type,
                        EventHeaders.EVENT_VERSION to CollectorIngressContract.VERSION.toString(),
                        EventHeaders.PRODUCER to PRODUCER,
                        EventHeaders.AGGREGATE_TYPE to CollectorIngressContract.AGGREGATE_TYPE,
                        EventHeaders.AGGREGATE_ID to aggregateId.toString(),
                        EventHeaders.OCCURRED_AT to occurredAt.toString(),
                    ),
            ),
        )
    }

    private companion object {
        const val PRODUCER = "realtime-service"
    }
}
