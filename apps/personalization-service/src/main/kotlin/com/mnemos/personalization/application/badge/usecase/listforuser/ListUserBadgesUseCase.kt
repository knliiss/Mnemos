package com.mnemos.personalization.application.badge.usecase.listforuser

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.badge.model.UserBadgeView

interface ListUserBadgesUseCase {
    fun execute(userId: UserId): List<UserBadgeView>
}
