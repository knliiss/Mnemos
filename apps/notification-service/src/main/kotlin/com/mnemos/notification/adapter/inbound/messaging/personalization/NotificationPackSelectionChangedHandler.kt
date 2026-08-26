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
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplateCondition
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplateVariant
import com.mnemos.notification.application.notification.personalization.model.NotificationPackValueForms
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

            if (envelope.metadata.actorUserId != envelope.payload.userId) {
                throw KafkaMessageProtocolException(
                    "Notification pack selection actor must match userId",
                )
            }

            if (envelope.occurredAt != envelope.payload.occurredAt) {
                throw KafkaMessageProtocolException(
                    "Notification pack selection occurredAt must match envelope occurredAt",
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

            envelope.payload.pack?.let { snapshot ->
                val templateTypes = snapshot.templates.map { template -> template.type.name }
                val expectedTypes = NotificationType.entries.map { type -> type.name }.toSet()

                if (templateTypes.size != expectedTypes.size || templateTypes.toSet() != expectedTypes) {
                    throw KafkaMessageProtocolException(
                        "Notification pack selection must define every notification type exactly once",
                    )
                }

                snapshot.templates.forEach { template ->
                    template.variants.forEach { variant ->
                        variant.conditions.forEach { condition ->
                            if (condition.field.templateType != template.type || condition.value !in condition.field.allowedValues) {
                                throw KafkaMessageProtocolException(
                                    "Notification pack selection contains an invalid template variant condition",
                                )
                            }
                        }
                    }
                }
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
                                    variants =
                                        template.variants.map { variant ->
                                            NotificationPackTemplateVariant(
                                                id = variant.id,
                                                conditions =
                                                    variant.conditions.map { condition ->
                                                        NotificationPackTemplateCondition(
                                                            field = condition.field,
                                                            value = condition.value,
                                                        )
                                                    },
                                                emoji = variant.emoji,
                                                soloTitle = variant.soloTitle,
                                                soloBody = variant.soloBody,
                                                compact = variant.compact,
                                                aggregateTitle = variant.aggregateTitle,
                                            )
                                        },
                                )
                        },
                    valueMappings =
                        snapshot.valueMappings.mapValues { (_, mappings) ->
                            mappings.mapValues { (_, forms) ->
                                NotificationPackValueForms(
                                    default = forms.default,
                                    forms = forms.forms,
                                )
                            }
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
