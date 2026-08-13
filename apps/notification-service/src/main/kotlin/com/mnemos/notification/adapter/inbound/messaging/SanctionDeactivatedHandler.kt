package com.mnemos.notification.adapter.inbound.messaging

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.moderation.contracts.ModerationEventContract
import com.mnemos.moderation.contracts.ModerationEventType
import com.mnemos.moderation.contracts.event.SanctionStateV1
import com.mnemos.moderation.contracts.shared.SanctionType
import com.mnemos.notification.adapter.out.persistence.moderation.NotificationSuspensionRepository
import org.springframework.stereotype.Component

@Component
class SanctionDeactivatedHandler(
    private val repository: NotificationSuspensionRepository,
) : KafkaMessageHandler<SanctionStateV1> {
    override val definition: KafkaMessageDefinition<SanctionStateV1> =
        kafkaMessageDefinition(
            topic = ModerationEventContract.TOPIC,
            type = ModerationEventType.SANCTION_DEACTIVATED,
            version = ModerationEventContract.VERSION,
            aggregateType = ModerationEventContract.AGGREGATE_TYPE,
        )

    override val consumer = "notification-service.sanction-deactivated"

    override fun handle(message: ConsumedMessage<SanctionStateV1>) {
        if (message.payload.type == SanctionType.NOTIFICATION_SUSPENSION) {
            repository.deactivate(
                sanctionId = message.payload.sanctionId,
                userId = message.payload.userId,
                occurredAt = message.payload.occurredAt,
            )
        }
    }
}
