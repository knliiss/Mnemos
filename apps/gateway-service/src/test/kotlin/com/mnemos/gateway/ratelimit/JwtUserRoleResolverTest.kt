package com.mnemos.gateway.ratelimit

import com.mnemos.common.core.model.user.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JwtUserRoleResolverTest {
    private val resolver = JwtUserRoleResolver()

    @Test
    fun `highest known jwt role wins`() {
        val realmAccess =
            mapOf(
                "roles" to listOf("USER", "MODERATOR", "SUPER_ADMIN", "ADMIN"),
            )

        val role = resolver.resolve(realmAccess)

        assertEquals(UserRole.SUPER_ADMIN, role)
    }

    @Test
    fun `unknown or missing roles fall back to user`() {
        assertEquals(
            UserRole.USER,
            resolver.resolve(mapOf("roles" to listOf("offline_access"))),
        )
        assertEquals(
            UserRole.USER,
            resolver.resolve(null),
        )
    }
}
