package com.mnemos.gateway.ratelimit

import com.mnemos.common.core.model.user.UserRole
import org.springframework.stereotype.Component

@Component
class GatewayRateLimitPolicy(
    private val properties: GatewayRateLimitProperties,
) {
    fun global(identity: RateLimitIdentity): RateLimitRule =
        when (identity.role) {
            null -> properties.anonymous
            UserRole.USER,
            UserRole.PERSONALIZATION_CREATOR,
            UserRole.PERSONALIZATION_MODERATOR,
            -> properties.user
            UserRole.MODERATOR -> properties.moderator
            UserRole.ADMIN -> properties.admin
            UserRole.SUPER_ADMIN -> properties.superAdmin
        }

    fun expensive(identity: RateLimitIdentity): RateLimitRule = global(identity).dividedBy(properties.expensiveRequestDivisor)
}
