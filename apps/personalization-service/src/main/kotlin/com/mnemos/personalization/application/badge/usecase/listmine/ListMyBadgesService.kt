package com.mnemos.personalization.application.badge.usecase.listmine

import com.mnemos.personalization.application.badge.model.UserBadgeView
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListMyBadgesService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: BadgeRepository,
) : ListMyBadgesUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<UserBadgeView> {
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
