package com.mnemos.personalization.application.badge.usecase.grant

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge

interface GrantBadgeUseCase {
    fun execute(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge
}
