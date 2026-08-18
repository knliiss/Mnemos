package com.mnemos.personalization.application.badge.usecase.revoke

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
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class RevokeBadgeServiceTest {
    private val now = Instant.parse("2026-08-15T20:00:00Z")

    @Test
    fun `revoke locks badge definition before assignment`() {
        val repository = mock<BadgeRepository>()
        val service =
            RevokeBadgeService(
                support = support(repository),
                repository = repository,
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
        whenever(repository.findUserBadgeForUpdate(userId, badge.id)).thenReturn(existing)

        service.execute(
            userId = userId,
            badgeId = badge.id,
        )

        val order = inOrder(repository)

        order.verify(repository).findByIdForUpdate(badge.id)
        order.verify(repository).findUserBadgeForUpdate(userId, badge.id)
        order.verify(repository).deleteUserBadge(userId, badge.id)
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
