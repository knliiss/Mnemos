package com.mnemos.gateway.ratelimit

import com.mnemos.common.core.model.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GatewayRateLimitPolicyTest {
    private val properties = GatewayRateLimitProperties()
    private val policy = GatewayRateLimitPolicy(properties)

    @Test
    fun `anonymous uses anonymous rule`() {
        val identity = RateLimitIdentity(key = "ip:127.0.0.1", role = null)

        val rule = policy.global(identity)

        assertEquals(RateLimitRule(30, 10), rule)
    }

    @Test
    fun `user uses user rule`() {
        val identity = RateLimitIdentity(key = "user:1", role = UserRole.USER)

        val rule = policy.global(identity)

        assertEquals(RateLimitRule(120, 40), rule)
    }

    @Test
    fun `moderator uses moderator rule`() {
        val identity = RateLimitIdentity(key = "user:1", role = UserRole.MODERATOR)

        val rule = policy.global(identity)

        assertEquals(RateLimitRule(240, 80), rule)
    }

    @Test
    fun `admin uses admin rule`() {
        val identity = RateLimitIdentity(key = "user:1", role = UserRole.ADMIN)

        val rule = policy.global(identity)

        assertEquals(RateLimitRule(360, 120), rule)
    }

    @Test
    fun `super admin uses super admin rule`() {
        val identity = RateLimitIdentity(key = "user:1", role = UserRole.SUPER_ADMIN)

        val rule = policy.global(identity)

        assertEquals(RateLimitRule(600, 200), rule)
    }

    @Test
    fun `expensive rule is additionally restricted`() {
        val identity = RateLimitIdentity(key = "user:1", role = UserRole.USER)

        val rule = policy.expensive(identity)

        assertEquals(RateLimitRule(30, 10), rule)
    }
}
