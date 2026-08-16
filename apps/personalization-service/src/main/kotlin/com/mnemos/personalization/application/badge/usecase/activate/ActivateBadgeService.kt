package com.mnemos.personalization.application.badge.usecase.activate

import com.mnemos.personalization.application.badge.exception.BadgeNotOwnedException
import com.mnemos.personalization.application.badge.model.UserBadgeView
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivateBadgeService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: BadgeRepository,
) : ActivateBadgeUseCase {
    @Transactional
    override fun execute(badgeId: BadgeId): UserBadgeView {
        val actor = actorProvider.requireActor()
        val badge = repository.findById(badgeId) ?: throw BadgeNotOwnedException()
        val userBadge =
            repository
                .findUserBadgesForUpdate(actor.userId)
                .firstOrNull { it.badgeId == badgeId }
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
}
