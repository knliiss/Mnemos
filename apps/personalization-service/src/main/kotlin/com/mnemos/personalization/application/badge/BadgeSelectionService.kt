package com.mnemos.personalization.application.badge

import com.mnemos.personalization.application.badge.exception.BadgeNotOwnedException
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

data class UserBadgeView(
    val badge: Badge,
    val grantedAt: Instant,
    val active: Boolean,
)

@Service
class BadgeSelectionService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: BadgeRepository,
) {
    @Transactional
    fun activate(badgeId: BadgeId): UserBadgeView {
        val actor = actorProvider.requireActor()
        val badge = repository.findById(badgeId) ?: throw BadgeNotOwnedException()
        val userBadge =
            repository.findUserBadge(actor.userId, badgeId)
                ?: throw BadgeNotOwnedException()

        repository.deactivateActiveUserBadge(actor.userId)
        userBadge.activate()

        val saved = repository.saveUserBadge(userBadge)

        return UserBadgeView(
            badge = badge,
            grantedAt = saved.grantedAt,
            active = saved.active,
        )
    }

    @Transactional
    fun deactivateActive() {
        val actor = actorProvider.requireActor()

        repository.deactivateActiveUserBadge(actor.userId)
    }

    fun listMine(): List<UserBadgeView> {
        val actor = actorProvider.requireActor()

        return repository.findUserBadges(actor.userId).mapNotNull { userBadge ->
            repository.findById(userBadge.badgeId)?.let { badge ->
                UserBadgeView(
                    badge = badge,
                    grantedAt = userBadge.grantedAt,
                    active = userBadge.active,
                )
            }
        }
    }
}
