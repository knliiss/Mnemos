package com.mnemos.personalization.application.badge.model

import com.mnemos.personalization.domain.badge.model.Badge
import java.time.Instant

data class UserBadgeView(
    val badge: Badge,
    val grantedAt: Instant,
    val active: Boolean,
)
