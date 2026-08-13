package com.mnemos.gateway.ratelimit

import com.mnemos.common.core.model.user.UserRole

data class RateLimitIdentity(
    val key: String,
    val role: UserRole?,
)
