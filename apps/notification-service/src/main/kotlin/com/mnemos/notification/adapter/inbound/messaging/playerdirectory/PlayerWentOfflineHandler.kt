package com.mnemos.notification.adapter.inbound.messaging.playerdirectory

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerWentOfflineV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.adapter.inbound.messaging.playerdirectory.PlayerWentOfflineEventMapper.toCommand
import com.mnemos.notification.application.playerpresence.usecase.process.ProcessPlayerWentOfflineEventUseCase
import org.springframework.stereotype.Component

@Component
class PlayerWentOfflineHandler(
    private val useCase: ProcessPlayerWentOfflineEventUseCase,
) : KafkaMessageHandler<PlayerWentOfflineV1> {
    override val definition =
        kafkaMessageDefinition<PlayerWentOfflineV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.WENT_OFFLINE,
            PlayerDirectoryEventContract.VERSION,
            PlayerDirectoryEventAggregateType.PLAYER,
        ) { envelope ->
            if (envelope.producer != PLAYER_DIRECTORY_SERVICE) {
                throw KafkaMessageProtocolException(
                    "Player offline events must be produced by player-directory-service",
                )
            }

            if (envelope.aggregateId != envelope.payload.playerId) {
                throw KafkaMessageProtocolException(
                    "Player offline event aggregate must match playerId",
                )
            }

            if (envelope.occurredAt != envelope.payload.observedAt) {
                throw KafkaMessageProtocolException(
                    "Player offline event occurredAt must match payload observedAt",
                )
            }
        }

    override val consumer = "notification-service.player-went-offline"

    override fun handle(message: ConsumedMessage<PlayerWentOfflineV1>) {
        useCase.execute(message.payload.toCommand(message.messageId))
    }

    private companion object {
        const val PLAYER_DIRECTORY_SERVICE = "player-directory-service"
    }
}
