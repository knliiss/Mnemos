package com.mnemos.personalization.domain.badge.model

import com.mnemos.common.core.model.id.UserId
import java.time.Instant

class UserBadge(
    val userId: UserId,
    val badgeId: BadgeId,
    val grantedAt: Instant,
    active: Boolean,
) {
    var active: Boolean = active
        private set

    fun activate() {
        active = true
    }

    fun deactivate() {
        active = false
    }
}
