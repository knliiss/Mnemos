package com.mnemos.notification.adapter.inbound.messaging.playerdirectory

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PresenceSubscriptionDeletedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.application.playerpresence.usecase.remove.RemovePlayerPresenceSubscriptionProjectionUseCase
import org.springframework.stereotype.Component

@Component
class PresenceSubscriptionDeletedHandler(
    private val useCase: RemovePlayerPresenceSubscriptionProjectionUseCase,
) : KafkaMessageHandler<PresenceSubscriptionDeletedV1> {
    override val definition =
        kafkaMessageDefinition<PresenceSubscriptionDeletedV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.PRESENCE_SUBSCRIPTION_DELETED,
            PlayerDirectoryEventContract.VERSION,
            PlayerDirectoryEventAggregateType.PLAYER,
        ) { envelope ->
            requirePlayerDirectoryEnvelope(
                producer = envelope.producer,
                aggregateId = envelope.aggregateId,
                playerId = envelope.payload.playerId,
                occurredAt = envelope.occurredAt,
                payloadOccurredAt = envelope.payload.occurredAt,
            )
        }

    override val consumer = "notification-service.player-presence-subscription-deleted"

    override fun handle(message: ConsumedMessage<PresenceSubscriptionDeletedV1>) {
        useCase.execute(message.payload.subscriptionId)
    }
}
