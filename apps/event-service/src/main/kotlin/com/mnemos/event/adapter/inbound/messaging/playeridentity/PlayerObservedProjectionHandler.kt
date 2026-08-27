package com.mnemos.event.adapter.inbound.messaging.playeridentity

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerObservedV1
import com.mnemos.event.application.playeridentity.usecase.project.ProjectPlayerIdentityCommand
import com.mnemos.event.application.playeridentity.usecase.project.ProjectPlayerIdentityUseCase
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import org.springframework.stereotype.Component

@Component
class PlayerObservedProjectionHandler(
    private val projectPlayerIdentityUseCase: ProjectPlayerIdentityUseCase,
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

    override val consumer = "event-service.player-observed"

    override fun handle(message: ConsumedMessage<PlayerObservedV1>) {
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
