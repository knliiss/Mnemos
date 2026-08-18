package com.mnemos.personalization.application.badge.usecase.listmine

import com.mnemos.personalization.application.badge.model.UserBadgeView

interface ListMyBadgesUseCase {
    fun execute(): List<UserBadgeView>
}
