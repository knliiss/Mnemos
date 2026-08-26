package com.mnemos.playerdirectory.application.subscription.usecase.list

import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.application.subscription.model.PresenceSubscriptionView
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListPresenceSubscriptionsService(
    private val subscriptionRepository: PresenceSubscriptionRepository,
    private val playerRepository: PlayerRepository,
) : ListPresenceSubscriptionsUseCase {
    @Transactional(readOnly = true)
    override fun execute(subscriberUserId: UUID): List<PresenceSubscriptionView> {
        val subscriptions = subscriptionRepository.findAllBySubscriberUserId(subscriberUserId)
        val players = playerRepository.findByIds(subscriptions.map { subscription -> subscription.playerId })

        return subscriptions.mapNotNull { subscription ->
            val player = players[subscription.playerId] ?: return@mapNotNull null

            PresenceSubscriptionView(
                id = subscription.id,
                playerId = subscription.playerId,
                nickname = player.nickname.value,
                presenceStatus = player.presence.status,
                realm = player.presence.realm,
                lastSeenOnline = player.presence.lastSeenOnline,
                createdAt = subscription.createdAt,
            )
        }
    }
}
