package com.mnemos.personalization.application.badge.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.personalization.application.badge.exception.BadgeAlreadyExistsException
import com.mnemos.personalization.application.badge.model.CreateBadgeCommand
import com.mnemos.personalization.application.badge.support.BadgeAdministrationSupport
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CreateBadgeServiceTest {
    private val clock =
        Clock.fixed(
            Instant.parse("2026-08-15T20:00:00Z"),
            ZoneOffset.UTC,
        )

    @Test
    fun `concurrent badge code conflict returns application conflict`() {
        val repository = mock<BadgeRepository>()
        val service =
            CreateBadgeService(
                support = support(repository),
                repository = repository,
                clock = clock,
            )

        whenever(repository.findByCode("EARLY_SUPPORTER")).thenReturn(null)
        whenever(repository.save(any())).thenThrow(
            DataIntegrityViolationException("duplicate badge code"),
        )

        assertThrows(BadgeAlreadyExistsException::class.java) {
            service.execute(
                CreateBadgeCommand(
                    code = "EARLY_SUPPORTER",
                    name = "Early Supporter",
                    icon = "star",
                    color = HexColor("#FFFFFF"),
                ),
            )
        }
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

    private class FixedActorProvider(
        private val actor: PersonalizationActor,
    ) : CurrentPersonalizationActorProvider {
        override fun requireActor(): PersonalizationActor = actor
    }
}
