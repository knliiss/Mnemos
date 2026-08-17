package com.mnemos.personalization.application.badge.usecase.revoke

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.badge.exception.BadgeNotFoundException
import com.mnemos.personalization.application.badge.support.BadgeAdministrationSupport
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RevokeBadgeService(
    private val support: BadgeAdministrationSupport,
    private val repository: BadgeRepository,
) : RevokeBadgeUseCase {
    @Transactional
    override fun execute(
        userId: UserId,
        badgeId: BadgeId,
    ) {
        support.requireAdministrator()
        support.requireBadgeForUpdate(badgeId)

        if (repository.findUserBadgeForUpdate(userId, badgeId) == null) {
            throw BadgeNotFoundException()
        }

        repository.deleteUserBadge(userId, badgeId)
    }
}
