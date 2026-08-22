package com.mnemos.personalization.application.badge.usecase.getimage

import com.mnemos.personalization.application.badge.model.BadgeImageContent
import com.mnemos.personalization.domain.badge.model.BadgeId

interface GetBadgeImageUseCase {
    fun execute(badgeId: BadgeId): BadgeImageContent
}
