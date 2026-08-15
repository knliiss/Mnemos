package com.mnemos.notification.adapter.inbound.messaging.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate
import com.mnemos.notification.application.notification.personalization.usecase.UpdateNotificationPackSelectionCommand
import com.mnemos.notification.application.notification.personalization.usecase.UpdateNotificationPackSelectionService
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.NotificationPackSelectionChangedV1
import org.springframework.stereotype.Component

@Component
class NotificationPackSelectionChangedHandler(
    private val service: UpdateNotificationPackSelectionService,
) : KafkaMessageHandler<NotificationPackSelectionChangedV1> {
    override val definition: KafkaMessageDefinition<NotificationPackSelectionChangedV1> =
        kafkaMessageDefinition(
            topic = PersonalizationEventContract.TOPIC,
            type = PersonalizationEventType.NOTIFICATION_PACK_SELECTION_CHANGED,
            version = PersonalizationEventContract.VERSION,
            aggregateType = PersonalizationAggregateType.USER_NOTIFICATION_PACK_SELECTION,
        ) { envelope ->
            if (envelope.producer != PERSONALIZATION_SERVICE) {
                throw KafkaMessageProtocolException(
                    "Notification pack selections must be produced by personalization-service",
                )
            }

            if (envelope.aggregateId != envelope.payload.userId) {
                throw KafkaMessageProtocolException(
                    "Notification pack selection aggregate must match userId",
                )
            }

            val hasPersonalization = envelope.payload.personalizationId != null
            val hasPack = envelope.payload.pack != null

            if (hasPersonalization != hasPack) {
                throw KafkaMessageProtocolException(
                    "Notification pack selection must contain both personalizationId and pack, or neither",
                )
            }

            if (envelope.payload.selectionRevision <= 0) {
                throw KafkaMessageProtocolException(
                    "Notification pack selection revision must be positive",
                )
            }
        }

    override val consumer = "notification-service.notification-pack-selection-changed"

    override fun handle(message: ConsumedMessage<NotificationPackSelectionChangedV1>) {
        val payload = message.payload
        val userId = UserId(payload.userId)
        val pack =
            payload.pack?.let { snapshot ->
                val personalizationId = requireNotNull(payload.personalizationId)

                ActiveNotificationPack(
                    userId = userId,
                    personalizationId = personalizationId,
                    mixedAggregateTitle = snapshot.mixedAggregateTitle,
                    templates =
                        snapshot.templates.associate { template ->
                            NotificationType.valueOf(template.type.name) to
                                NotificationPackTemplate(
                                    emoji = template.emoji,
                                    soloTitle = template.soloTitle,
                                    soloBody = template.soloBody,
                                    compact = template.compact,
                                    aggregateTitle = template.aggregateTitle,
                                )
                        },
                )
            }

        service.execute(
            UpdateNotificationPackSelectionCommand(
                userId = userId,
                pack = pack,
                selectionRevision = payload.selectionRevision,
                eventId = message.messageId,
                occurredAt = message.occurredAt,
            ),
        )
    }

    private companion object {
        const val PERSONALIZATION_SERVICE = "personalization-service"
    }
}
