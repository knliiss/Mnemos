package com.mnemos.moderation.adapter.inbound.http.admin.leaderboard

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateLeaderboardExclusionRequestTest {
    @Test
    fun `account exclusions accept only all and coins scopes`() {
        assertSupported(LeaderboardExclusionSubjectType.ACCOUNT, LeaderboardExclusionScope.ALL)
        assertSupported(LeaderboardExclusionSubjectType.ACCOUNT, LeaderboardExclusionScope.COINS)
        assertUnsupported(LeaderboardExclusionSubjectType.ACCOUNT, LeaderboardExclusionScope.DROPS)
        assertUnsupported(LeaderboardExclusionSubjectType.ACCOUNT, LeaderboardExclusionScope.BOOSTERS)
    }

    @Test
    fun `player exclusions accept all drops and boosters scopes`() {
        assertSupported(LeaderboardExclusionSubjectType.PLAYER, LeaderboardExclusionScope.ALL)
        assertSupported(LeaderboardExclusionSubjectType.PLAYER, LeaderboardExclusionScope.DROPS)
        assertSupported(LeaderboardExclusionSubjectType.PLAYER, LeaderboardExclusionScope.BOOSTERS)
        assertUnsupported(LeaderboardExclusionSubjectType.PLAYER, LeaderboardExclusionScope.COINS)
    }

    private fun assertSupported(
        subjectType: LeaderboardExclusionSubjectType,
        scope: LeaderboardExclusionScope,
    ) {
        assertTrue(violations(subjectType, scope).isEmpty())
    }

    private fun assertUnsupported(
        subjectType: LeaderboardExclusionSubjectType,
        scope: LeaderboardExclusionScope,
    ) {
        val violations = violations(subjectType, scope)

        assertEquals(1, violations.size)
        assertEquals("subjectScopeSupported", violations.single().propertyPath.toString())
    }

    private fun violations(
        subjectType: LeaderboardExclusionSubjectType,
        scope: LeaderboardExclusionScope,
    ) = Validation.buildDefaultValidatorFactory().use { validatorFactory ->
        validatorFactory.validator.validate(
            CreateLeaderboardExclusionRequest(
                subjectType = subjectType,
                subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000801"),
                scope = scope,
                reason = null,
            ),
        )
    }
}
