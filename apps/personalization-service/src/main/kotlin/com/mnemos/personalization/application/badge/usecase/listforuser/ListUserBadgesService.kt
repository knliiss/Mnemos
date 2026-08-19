package com.mnemos.personalization.application.badge.usecase.listforuser

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.badge.model.UserBadgeView
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListUserBadgesService(
    private val repository: BadgeRepository,
) : ListUserBadgesUseCase {
    @Transactional(readOnly = true)
    override fun execute(userId: UserId): List<UserBadgeView> =
        repository.findUserBadges(userId).mapNotNull { userBadge ->
            repository.findById(userBadge.badgeId)?.let { badge ->
                UserBadgeView(
                    badge = badge,
                    grantedAt = userBadge.grantedAt,
                    active = userBadge.active,
                )
            }
        }
}
