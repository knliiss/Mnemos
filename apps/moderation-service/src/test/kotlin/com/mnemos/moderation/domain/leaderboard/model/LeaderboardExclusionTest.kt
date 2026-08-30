package com.mnemos.moderation.domain.leaderboard.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LeaderboardExclusionTest {
    @Test
    fun `account exclusion rejects a drop scope`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                create(
                    subjectType = LeaderboardExclusionSubjectType.ACCOUNT,
                    scope = LeaderboardExclusionScope.DROPS,
                )
            }

        assertEquals(
            "Scope DROPS is not supported for leaderboard exclusion subject type ACCOUNT",
            exception.message,
        )
    }

    @Test
    fun `player exclusion rejects a coin scope`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                create(
                    subjectType = LeaderboardExclusionSubjectType.PLAYER,
                    scope = LeaderboardExclusionScope.COINS,
                )
            }

        assertEquals(
            "Scope COINS is not supported for leaderboard exclusion subject type PLAYER",
            exception.message,
        )
    }

    private fun create(
        subjectType: LeaderboardExclusionSubjectType,
        scope: LeaderboardExclusionScope,
    ): LeaderboardExclusion =
        LeaderboardExclusion.create(
            subjectType = subjectType,
            subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000802"),
            scope = scope,
            reason = null,
            createdByUserId = UserId(UUID.fromString("019c1156-3274-7000-8000-000000000803")),
            createdAt = Instant.parse("2026-08-30T00:00:00Z"),
        )
}
