package com.mnemos.personalization.application.badge.usecase.grant

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.personalization.application.badge.support.BadgeAdministrationSupport
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class GrantBadgeServiceTest {
    private val now = Instant.parse("2026-08-15T20:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `grant returns existing assignment after locking badge definition`() {
        val repository = mock<BadgeRepository>()
        val service =
            GrantBadgeService(
                support = support(repository),
                repository = repository,
                clock = clock,
            )
        val userId = UserId.generate()
        val badge = badge()
        val existing =
            UserBadge(
                userId = userId,
                badgeId = badge.id,
                grantedAt = now.minusSeconds(60),
                active = false,
            )

        whenever(repository.findByIdForUpdate(badge.id)).thenReturn(badge)
        whenever(repository.findUserBadge(userId, badge.id)).thenReturn(existing)

        val result =
            service.execute(
                userId = userId,
                badgeId = badge.id,
            )

        assertEquals(existing, result)
        verify(repository).findByIdForUpdate(badge.id)
        verify(repository, never()).saveUserBadge(any())
    }

    private fun support(repository: BadgeRepository): BadgeAdministrationSupport =
        BadgeAdministrationSupport(
            actorProvider =
                FixedActorProvider(
                    PersonalizationActor(
                        userId = UserId.generate(),
                        roles = setOf(UserRole.ADMIN),
                    ),
                ),
            repository = repository,
        )

    private fun badge(): Badge =
        Badge(
            id = BadgeId.generate(),
            code = "EARLY_SUPPORTER",
            name = "Early Supporter",
            icon = "star",
            color = HexColor("#FFFFFF"),
            createdAt = now.minusSeconds(120),
        )

    private class FixedActorProvider(
        private val actor: PersonalizationActor,
    ) : CurrentPersonalizationActorProvider {
        override fun requireActor(): PersonalizationActor = actor
    }
}
