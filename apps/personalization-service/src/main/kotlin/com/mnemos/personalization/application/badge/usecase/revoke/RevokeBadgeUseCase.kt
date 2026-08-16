package com.mnemos.personalization.application.badge.usecase.revoke

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.badge.model.BadgeId

interface RevokeBadgeUseCase {
    fun execute(
        userId: UserId,
        badgeId: BadgeId,
    )
}
