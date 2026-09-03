package com.mnemos.realtime.adapter.inbound.messaging.moderation

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.moderation.contracts.ModerationEventContract
import com.mnemos.moderation.contracts.ModerationEventType
import com.mnemos.moderation.contracts.event.SanctionStateV1
import com.mnemos.moderation.contracts.shared.SanctionStatus
import com.mnemos.realtime.application.delivery.model.SanctionChangedPayload
import org.springframework.stereotype.Component

@Component
class SanctionActivatedHandler(
    private val publisher: ModerationRealtimeEventPublisher,
) : KafkaMessageHandler<SanctionStateV1> {
    override val definition =
        kafkaMessageDefinition<SanctionStateV1>(
            topic = ModerationEventContract.TOPIC,
            type = ModerationEventType.SANCTION_ACTIVATED,
            version = ModerationEventContract.VERSION,
            aggregateType = ModerationEventContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != MODERATION_SERVICE) {
                throw KafkaMessageProtocolException("Sanction events must be produced by moderation-service")
            }
            if (envelope.payload.status != SanctionStatus.ACTIVE) {
                throw KafkaMessageProtocolException("Sanction activation event must carry ACTIVE status")
            }
        }

    override val consumer = "realtime-service.sanction-activated"

    override fun handle(message: ConsumedMessage<SanctionStateV1>) {
        val payload = message.payload

        publisher.publish(
            message = message,
            userId = payload.userId,
            payload =
                SanctionChangedPayload(
                    sanctionId = payload.sanctionId,
                    userId = payload.userId,
                    type = payload.type.name,
                    status = payload.status.name,
                ),
        )
    }

    private companion object {
        const val MODERATION_SERVICE = "moderation-service"
    }
}
