package com.mnemos.event.adapter.inbound.messaging.playeridentity

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerNicknameChangedV1
import com.mnemos.event.application.playeridentity.usecase.project.ProjectPlayerIdentityCommand
import com.mnemos.event.application.playeridentity.usecase.project.ProjectPlayerIdentityUseCase
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import org.springframework.stereotype.Component

@Component
class PlayerNicknameChangedProjectionHandler(
    private val projectPlayerIdentityUseCase: ProjectPlayerIdentityUseCase,
) : KafkaMessageHandler<PlayerNicknameChangedV1> {
    override val definition =
        kafkaMessageDefinition<PlayerNicknameChangedV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.NICKNAME_CHANGED,
            PlayerDirectoryEventContract.VERSION,
            PlayerDirectoryEventAggregateType.PLAYER,
        ) { envelope ->
            if (envelope.producer != PLAYER_DIRECTORY_SERVICE) {
                throw KafkaMessageProtocolException("Player nickname changes must be produced by player-directory-service")
            }

            if (envelope.aggregateId != envelope.payload.playerId) {
                throw KafkaMessageProtocolException("Player nickname aggregateId must match playerId")
            }

            if (envelope.occurredAt != envelope.payload.observedAt) {
                throw KafkaMessageProtocolException("Player nickname occurredAt must match observedAt")
            }
        }

    override val consumer = "event-service.player-nickname-changed"

    override fun handle(message: ConsumedMessage<PlayerNicknameChangedV1>) {
        projectPlayerIdentityUseCase.execute(
            ProjectPlayerIdentityCommand(
                playerId = message.payload.playerId,
                nickname = message.payload.nickname,
                externalVersion = message.payload.externalVersion,
                observedAt = message.payload.observedAt,
            ),
        )
    }

    private companion object {
        const val PLAYER_DIRECTORY_SERVICE = "player-directory-service"
    }
}
