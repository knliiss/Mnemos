package com.mnemos.account.adapter.inbound.messaging.player

import com.mnemos.account.application.usecase.player.SyncPlayerNicknameCommand
import com.mnemos.account.application.usecase.player.SyncPlayerNicknameUseCase
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerObservedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import org.springframework.stereotype.Component

/**
 * The first API observation may already contain a renamed player.
 * Account identity must converge even when there is no prior
 * player-directory state to generate a NICKNAME_CHANGED event.
 */
@Component
class PlayerObservedHandler(
    private val useCase: SyncPlayerNicknameUseCase,
) : KafkaMessageHandler<PlayerObservedV1> {
    override val definition =
        kafkaMessageDefinition<PlayerObservedV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.PLAYER_OBSERVED,
            PlayerDirectoryEventContract.VERSION,
            PlayerDirectoryEventAggregateType.PLAYER,
        ) { envelope ->
            if (envelope.producer != PLAYER_DIRECTORY_SERVICE) {
                throw KafkaMessageProtocolException("Player observations must be produced by player-directory-service")
            }
            if (envelope.aggregateId != envelope.payload.playerId) {
                throw KafkaMessageProtocolException("Player observation aggregateId must match playerId")
            }
            if (envelope.occurredAt != envelope.payload.observedAt) {
                throw KafkaMessageProtocolException("Player observation occurredAt must match observedAt")
            }
        }

    override val consumer = "account-service.player-observed"

    override fun handle(message: ConsumedMessage<PlayerObservedV1>) {
        val observation = message.payload
        useCase.execute(
            SyncPlayerNicknameCommand(
                playerId = observation.playerId,
                nickname = observation.nickname,
                externalVersion = observation.externalVersion,
                observedAt = observation.observedAt,
            ),
        )
    }

    private companion object {
        const val PLAYER_DIRECTORY_SERVICE = "player-directory-service"
    }
}
