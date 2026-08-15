package com.mnemos.social.adapter.inbound.messaging.personalization

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.NotificationPackSelectionChangedV1
import com.mnemos.social.application.spotlight.UpdateNotificationPackSelectionService
import org.springframework.stereotype.Component

@Component
class NotificationPackSelectionChangedHandler(
    private val service: UpdateNotificationPackSelectionService,
) : KafkaMessageHandler<NotificationPackSelectionChangedV1> {
    override val definition =
        kafkaMessageDefinition<NotificationPackSelectionChangedV1>(
            PersonalizationEventContract.TOPIC,
            PersonalizationEventType.NOTIFICATION_PACK_SELECTION_CHANGED,
            PersonalizationEventContract.VERSION,
            PersonalizationAggregateType.USER_NOTIFICATION_PACK_SELECTION,
        ) { envelope ->
            if (envelope.producer != PERSONALIZATION_SERVICE) {
                throw KafkaMessageProtocolException("Notification pack selection must be produced by personalization-service")
            }
            if (envelope.aggregateId != envelope.payload.userId) {
                throw KafkaMessageProtocolException("Notification pack selection aggregateId must match userId")
            }
            if (envelope.occurredAt != envelope.payload.occurredAt) {
                throw KafkaMessageProtocolException("Notification pack selection occurredAt must match envelope")
            }
        }

    override val consumer = "social-service.notification-pack-selection-changed"

    override fun handle(message: ConsumedMessage<NotificationPackSelectionChangedV1>) {
        service.execute(message.payload)
    }

    private companion object {
        const val PERSONALIZATION_SERVICE = "personalization-service"
    }
}
