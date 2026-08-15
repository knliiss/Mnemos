package com.mnemos.event.adapter.inbound.messaging.collector

import com.mnemos.event.application.collector.port.out.CollectorCoverageRecorder
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressType
import com.mnemos.realtime.contracts.collector.CollectorPresenceChangedV2
import org.springframework.stereotype.Component

@Component
class CollectorPresenceChangedHandler(
    private val coverageRecorder: CollectorCoverageRecorder,
) : KafkaMessageHandler<CollectorPresenceChangedV2> {
    override val definition =
        kafkaMessageDefinition<CollectorPresenceChangedV2>(
            CollectorIngressContract.TOPIC,
            CollectorIngressType.PRESENCE_CHANGED,
            CollectorIngressContract.VERSION,
            CollectorIngressContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != REALTIME_SERVICE) {
                throw KafkaMessageProtocolException("Collector presence must be produced by realtime-service")
            }

            if (envelope.aggregateId != CollectorIngressContract.PRESENCE_AGGREGATE_ID) {
                throw KafkaMessageProtocolException("Collector presence must use the stable presence aggregateId")
            }

            if (envelope.eventId != envelope.payload.changeId) {
                throw KafkaMessageProtocolException("Collector presence eventId must match changeId")
            }

            if (envelope.occurredAt != envelope.payload.occurredAt) {
                throw KafkaMessageProtocolException("Collector presence occurredAt must match its payload")
            }

            if (envelope.payload.activeCollectors < 0 || envelope.payload.revision < 0) {
                throw KafkaMessageProtocolException("Collector presence counters must not be negative")
            }
        }

    override val consumer = "event-service.collector-presence"

    override fun handle(message: ConsumedMessage<CollectorPresenceChangedV2>) {
        coverageRecorder.record(
            activeCollectors = message.payload.activeCollectors,
            occurredAt = message.payload.occurredAt,
            revision = message.payload.revision,
        )
    }

    private companion object {
        const val REALTIME_SERVICE = "realtime-service"
    }
}
