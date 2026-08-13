package com.mnemos.gateway.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.gateway.rate-limit")
data class GatewayRateLimitProperties(
    val anonymous: RateLimitRule = RateLimitRule(30, 10),
    val user: RateLimitRule = RateLimitRule(120, 40),
    val moderator: RateLimitRule = RateLimitRule(240, 80),
    val admin: RateLimitRule = RateLimitRule(360, 120),
    val superAdmin: RateLimitRule = RateLimitRule(600, 200),
    val expensiveRequestDivisor: Int = 4,
) {
    init {
        require(expensiveRequestDivisor > 0)
    }
}
