package com.mnemos.profile.adapter.inbound.messaging.player

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerNicknameChangedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.profile.application.profile.usecase.projectplayer.ProjectPlayerNicknameUseCase
import org.springframework.stereotype.Component

@Component
class PlayerNicknameChangedHandler(
    private val useCase: ProjectPlayerNicknameUseCase,
) : KafkaMessageHandler<PlayerNicknameChangedV1> {
    override val definition =
        kafkaMessageDefinition<PlayerNicknameChangedV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.NICKNAME_CHANGED,
            PlayerDirectoryEventContract.VERSION,
            PlayerDirectoryEventAggregateType.PLAYER,
        ) { envelope ->
            if (envelope.producer != PLAYER_DIRECTORY_SERVICE) {
                throw KafkaMessageProtocolException("Player nickname change must be produced by player-directory-service")
            }
        }

    override val consumer = "profile-service.player-nickname-changed"

    override fun handle(message: ConsumedMessage<PlayerNicknameChangedV1>) {
        val event = message.payload

        useCase.execute(
            playerId = event.playerId,
            nickname = event.nickname,
            externalVersion = event.externalVersion,
            observedAt = event.observedAt,
        )
    }

    private companion object {
        const val PLAYER_DIRECTORY_SERVICE = "player-directory-service"
    }
}
