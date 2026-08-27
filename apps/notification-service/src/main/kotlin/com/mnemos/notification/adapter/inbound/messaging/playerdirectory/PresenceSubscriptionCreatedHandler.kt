package com.mnemos.notification.adapter.inbound.messaging.playerdirectory

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PresenceSubscriptionCreatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.adapter.inbound.messaging.playerdirectory.PlayerPresenceSubscriptionEventMapper.toDomain
import com.mnemos.notification.application.playerpresence.usecase.project.ProjectPlayerPresenceSubscriptionUseCase
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class PresenceSubscriptionCreatedHandler(
    private val useCase: ProjectPlayerPresenceSubscriptionUseCase,
) : KafkaMessageHandler<PresenceSubscriptionCreatedV1> {
    override val definition =
        kafkaMessageDefinition<PresenceSubscriptionCreatedV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.PRESENCE_SUBSCRIPTION_CREATED,
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

    override val consumer = "notification-service.player-presence-subscription-created"

    override fun handle(message: ConsumedMessage<PresenceSubscriptionCreatedV1>) {
        useCase.execute(message.payload.toDomain())
    }
}

internal fun requirePlayerDirectoryEnvelope(
    producer: String,
    aggregateId: UUID,
    playerId: UUID,
    occurredAt: Instant,
    payloadOccurredAt: Instant,
) {
    if (producer != "player-directory-service") {
        throw KafkaMessageProtocolException("Player presence events must be produced by player-directory-service")
    }

    if (aggregateId != playerId) {
        throw KafkaMessageProtocolException("Player presence event aggregate must match playerId")
    }

    if (occurredAt != payloadOccurredAt) {
        throw KafkaMessageProtocolException("Player presence event occurredAt must match payload occurredAt")
    }
}
