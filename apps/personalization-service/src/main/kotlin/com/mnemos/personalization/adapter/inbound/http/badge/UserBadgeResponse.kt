package com.mnemos.personalization.adapter.inbound.http.badge

import java.time.Instant

data class UserBadgeResponse(
    val badge: BadgeResponse,
    val grantedAt: Instant,
    val active: Boolean,
)
