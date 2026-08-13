package com.mnemos.moderation.domain.sanction.policy

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.moderation.domain.sanction.exception.SanctionDurationExceededException
import com.mnemos.moderation.domain.sanction.exception.SanctionTargetAccessDeniedException
import com.mnemos.moderation.domain.sanction.exception.SelfSanctionException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class SanctionPolicyTest {
    private val now = Instant.parse("2026-08-13T16:00:00Z")

    @Test
    fun `moderator can sanction user for at most thirty days`() {
        assertDoesNotThrow {
            SanctionPolicy.validateCreation(
                actorUserId = UserId.generate(),
                actorRole = UserRole.MODERATOR,
                targetUserId = UserId.generate(),
                targetRole = UserRole.USER,
                createdAt = now,
                expiresAt = now.plusSeconds(30L * 24 * 60 * 60),
            )
        }
    }

    @Test
    fun `moderator cannot create permanent sanction`() {
        assertThrows(SanctionDurationExceededException::class.java) {
            SanctionPolicy.validateCreation(
                actorUserId = UserId.generate(),
                actorRole = UserRole.MODERATOR,
                targetUserId = UserId.generate(),
                targetRole = UserRole.USER,
                createdAt = now,
                expiresAt = null,
            )
        }
    }

    @Test
    fun `moderator cannot sanction moderator`() {
        assertThrows(SanctionTargetAccessDeniedException::class.java) {
            SanctionPolicy.validateCreation(
                actorUserId = UserId.generate(),
                actorRole = UserRole.MODERATOR,
                targetUserId = UserId.generate(),
                targetRole = UserRole.MODERATOR,
                createdAt = now,
                expiresAt = now.plusSeconds(60),
            )
        }
    }

    @Test
    fun `admin can permanently sanction moderator`() {
        assertDoesNotThrow {
            SanctionPolicy.validateCreation(
                actorUserId = UserId.generate(),
                actorRole = UserRole.ADMIN,
                targetUserId = UserId.generate(),
                targetRole = UserRole.MODERATOR,
                createdAt = now,
                expiresAt = null,
            )
        }
    }

    @Test
    fun `super admin can sanction admin`() {
        assertDoesNotThrow {
            SanctionPolicy.validateCreation(
                actorUserId = UserId.generate(),
                actorRole = UserRole.SUPER_ADMIN,
                targetUserId = UserId.generate(),
                targetRole = UserRole.ADMIN,
                createdAt = now,
                expiresAt = null,
            )
        }
    }

    @Test
    fun `admin cannot sanction super admin`() {
        assertThrows(SanctionTargetAccessDeniedException::class.java) {
            SanctionPolicy.validateCreation(
                actorUserId = UserId.generate(),
                actorRole = UserRole.ADMIN,
                targetUserId = UserId.generate(),
                targetRole = UserRole.SUPER_ADMIN,
                createdAt = now,
                expiresAt = now.plusSeconds(60),
            )
        }
    }

    @Test
    fun `moderator cannot sanction self`() {
        val userId = UserId.generate()

        assertThrows(SelfSanctionException::class.java) {
            SanctionPolicy.validateCreation(
                actorUserId = userId,
                actorRole = UserRole.MODERATOR,
                targetUserId = userId,
                targetRole = UserRole.USER,
                createdAt = now,
                expiresAt = now.plusSeconds(60),
            )
        }
    }
}
