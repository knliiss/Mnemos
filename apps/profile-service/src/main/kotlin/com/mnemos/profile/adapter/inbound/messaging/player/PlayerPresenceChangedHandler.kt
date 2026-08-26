package com.mnemos.profile.adapter.inbound.messaging.player

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerPresenceChangedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.profile.adapter.inbound.messaging.player.PlayerDirectoryProfileEventMapper.toDomain
import com.mnemos.profile.application.profile.usecase.projectplayer.ProjectPlayerPresenceUseCase
import org.springframework.stereotype.Component

@Component
class PlayerPresenceChangedHandler(
    private val useCase: ProjectPlayerPresenceUseCase,
) : KafkaMessageHandler<PlayerPresenceChangedV1> {
    override val definition =
        kafkaMessageDefinition<PlayerPresenceChangedV1>(
            PlayerDirectoryEventContract.TOPIC,
            PlayerDirectoryEventType.PRESENCE_CHANGED,
            PlayerDirectoryEventContract.VERSION,
            PlayerDirectoryEventAggregateType.PLAYER,
        ) { envelope ->
            if (envelope.producer != PLAYER_DIRECTORY_SERVICE) {
                throw KafkaMessageProtocolException("Player presence change must be produced by player-directory-service")
            }
        }

    override val consumer = "profile-service.player-presence-changed"

    override fun handle(message: ConsumedMessage<PlayerPresenceChangedV1>) {
        val event = message.payload

        useCase.execute(
            playerId = event.playerId,
            nickname = event.nickname,
            externalVersion = event.externalVersion,
            presenceStatus = event.status.toDomain(),
            realm = event.realm,
            realmType = event.realmType,
            privacyHidden = event.privacyHidden,
            lastSeenOnline = event.lastSeenOnline,
            observedAt = event.observedAt,
        )
    }

    private companion object {
        const val PLAYER_DIRECTORY_SERVICE = "player-directory-service"
    }
}
