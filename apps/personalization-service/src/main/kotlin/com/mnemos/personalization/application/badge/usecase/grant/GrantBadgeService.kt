package com.mnemos.personalization.application.badge.usecase.grant

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.badge.support.BadgeAdministrationSupport
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class GrantBadgeService(
    private val support: BadgeAdministrationSupport,
    private val repository: BadgeRepository,
    private val clock: Clock,
) : GrantBadgeUseCase {
    @Transactional
    override fun execute(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge {
        support.requireAdministrator()
        support.requireBadgeForUpdate(badgeId)

        repository.findUserBadge(userId, badgeId)?.let { existing ->
            return existing
        }

        return repository.saveUserBadge(
            UserBadge(
                userId = userId,
                badgeId = badgeId,
                grantedAt = clock.instant(),
                active = false,
            ),
        )
    }
}
