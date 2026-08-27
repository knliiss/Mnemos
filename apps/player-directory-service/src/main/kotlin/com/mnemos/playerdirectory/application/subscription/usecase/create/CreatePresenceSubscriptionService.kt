package com.mnemos.playerdirectory.application.subscription.usecase.create

import com.mnemos.playerdirectory.application.player.exception.PlayerNotFoundException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerGateway
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.application.subscription.exception.PlayerPresenceNotObservableException
import com.mnemos.playerdirectory.application.subscription.exception.PlayerPresenceVerificationUnavailableException
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import com.mnemos.playerdirectory.application.subscription.support.PresenceSubscriptionCoordinator
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import org.springframework.stereotype.Service

@Service
class CreatePresenceSubscriptionService(
    private val cristalixPlayerGateway: CristalixPlayerGateway,
    private val playerObservationCoordinator: PlayerObservationCoordinator,
    private val presenceSubscriptionRepository: PresenceSubscriptionRepository,
    private val presenceSubscriptionCoordinator: PresenceSubscriptionCoordinator,
) : CreatePresenceSubscriptionUseCase {
    override fun execute(command: CreatePresenceSubscriptionCommand): PresenceSubscription {
        val existing =
            presenceSubscriptionRepository.findBySubscriberUserIdAndPlayerId(
                subscriberUserId = command.subscriberUserId,
                playerId = command.playerId,
            )

        if (existing != null) {
            return existing
        }

        val observation = cristalixPlayerGateway.findById(command.playerId) ?: throw PlayerNotFoundException(command.playerId)

        when (observation.presence.status) {
            PlayerPresenceStatus.ONLINE,
            PlayerPresenceStatus.OFFLINE,
            -> Unit

            PlayerPresenceStatus.HIDDEN -> throw PlayerPresenceNotObservableException(command.playerId)
            PlayerPresenceStatus.UNKNOWN -> throw PlayerPresenceVerificationUnavailableException(command.playerId)
        }

        playerObservationCoordinator.apply(observation)

        return presenceSubscriptionCoordinator.createIfAbsent(
            subscriberUserId = command.subscriberUserId,
            playerId = command.playerId,
            createdAt = observation.presence.observedAt,
        )
    }
}
