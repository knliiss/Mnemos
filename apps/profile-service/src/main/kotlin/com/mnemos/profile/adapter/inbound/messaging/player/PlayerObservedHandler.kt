package com.mnemos.profile.adapter.inbound.messaging.player

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerObservedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.profile.adapter.inbound.messaging.player.PlayerDirectoryProfileEventMapper.toDomain
import com.mnemos.profile.application.profile.usecase.projectplayer.ProjectPlayerObservedUseCase
import org.springframework.stereotype.Component

@Component
class PlayerObservedHandler(
    private val useCase: ProjectPlayerObservedUseCase,
) : KafkaMessageHandler<PlayerObservedV1> {
    override val definition =
        kafkaMessageDefinition<PlayerObservedV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.PLAYER_OBSERVED,
            PlayerDirectoryEventContract.VERSION,
            PlayerDirectoryEventAggregateType.PLAYER,
        ) { envelope ->
            if (envelope.producer != PLAYER_DIRECTORY_SERVICE) {
                throw KafkaMessageProtocolException("Player observation must be produced by player-directory-service")
            }
        }

    override val consumer = "profile-service.player-observed"

    override fun handle(message: ConsumedMessage<PlayerObservedV1>) {
        useCase.execute(message.payload.toDomain())
    }

    private companion object {
        const val PLAYER_DIRECTORY_SERVICE = "player-directory-service"
    }
}
