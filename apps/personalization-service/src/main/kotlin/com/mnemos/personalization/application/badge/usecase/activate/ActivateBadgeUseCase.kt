package com.mnemos.personalization.application.badge.usecase.activate

import com.mnemos.personalization.application.badge.model.UserBadgeView
import com.mnemos.personalization.domain.badge.model.BadgeId

interface ActivateBadgeUseCase {
    fun execute(badgeId: BadgeId): UserBadgeView
}
