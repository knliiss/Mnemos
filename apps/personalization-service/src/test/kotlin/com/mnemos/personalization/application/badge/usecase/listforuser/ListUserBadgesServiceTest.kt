package com.mnemos.personalization.application.badge.usecase.listforuser

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.UserBadge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class ListUserBadgesServiceTest {
    @Test
    fun `returns badge details with assignment state`() {
        val repository = mock<BadgeRepository>()
        val userId = UserId.generate()
        val badge =
            Badge.create(
                code = "EVENT_MASTER",
                name = "Event Master",
                icon = "star",
                color = HexColor("#C7F24A"),
                now = Instant.parse("2026-08-19T18:00:00Z"),
            )
        val userBadge =
            UserBadge(
                userId = userId,
                badgeId = badge.id,
                grantedAt = Instant.parse("2026-08-19T18:05:00Z"),
                active = true,
            )
        val service = ListUserBadgesService(repository)

        whenever(repository.findUserBadges(userId)).thenReturn(listOf(userBadge))
        whenever(repository.findById(badge.id)).thenReturn(badge)

        val result = service.execute(userId)

        assertEquals(1, result.size)
        assertEquals(badge, result.single().badge)
        assertTrue(result.single().active)
    }
}
