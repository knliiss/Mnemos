package com.mnemos.moderation.application.sanction.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.moderation.application.security.model.ModeratorActor
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.application.security.port.out.UserRoleProvider
import com.mnemos.moderation.domain.sanction.exception.SelfSanctionException
import com.mnemos.moderation.domain.sanction.model.DomainSanctionStatus
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import com.mnemos.moderation.domain.sanction.model.Sanction
import com.mnemos.moderation.domain.sanction.repository.SanctionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class CreateSanctionServiceTest {
    @Test
    fun `self sanction is rejected before target role lookup`() {
        val now = Instant.parse("2026-08-13T18:00:00Z")
        val actorUserId = UserId(UUID.fromString("0198a000-0000-7000-8000-000000000001"))
        val repository = mock<SanctionRepository>()
        val currentModeratorProvider = mock<CurrentModeratorProvider>()
        val userRoleProvider = mock<UserRoleProvider>()
        val publisher = RecordingIntegrationEventPublisher()
        val service =
            CreateSanctionService(
                sanctionRepository = repository,
                currentModeratorProvider = currentModeratorProvider,
                userRoleProvider = userRoleProvider,
                publisher = publisher,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        whenever(currentModeratorProvider.current()).thenReturn(
            ModeratorActor(
                userId = actorUserId,
                role = UserRole.SUPER_ADMIN,
            ),
        )

        assertThrows(SelfSanctionException::class.java) {
            service.execute(
                CreateSanctionCommand(
                    userId = actorUserId,
                    type = DomainSanctionType.BAN,
                    reason = "Self sanction",
                    internalNote = null,
                    duration = null,
                ),
            )
        }

        verifyNoInteractions(userRoleProvider)
        verifyNoInteractions(repository)
        assertEquals(0, publisher.events.size)
    }

    @Test
    fun `create expires stale active sanction before creating replacement`() {
        val now = Instant.parse("2026-08-13T18:00:00Z")
        val actorUserId = UserId(UUID.fromString("0198a000-0000-7000-8000-000000000001"))
        val targetUserId = UserId(UUID.fromString("0198a000-0000-7000-8000-000000000002"))
        val staleSanction =
            Sanction.create(
                userId = targetUserId,
                type = DomainSanctionType.BAN,
                reason = "Previous sanction",
                internalNote = null,
                createdByUserId = actorUserId,
                createdAt = now.minus(Duration.ofHours(2)),
                expiresAt = now.minus(Duration.ofHours(1)),
            )
        val repository = mock<SanctionRepository>()
        val currentModeratorProvider = mock<CurrentModeratorProvider>()
        val userRoleProvider = mock<UserRoleProvider>()
        val publisher = RecordingIntegrationEventPublisher()
        val service =
            CreateSanctionService(
                sanctionRepository = repository,
                currentModeratorProvider = currentModeratorProvider,
                userRoleProvider = userRoleProvider,
                publisher = publisher,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        whenever(currentModeratorProvider.current()).thenReturn(
            ModeratorActor(
                userId = actorUserId,
                role = UserRole.ADMIN,
            ),
        )
        whenever(userRoleProvider.highestRole(targetUserId)).thenReturn(UserRole.USER)
        whenever(
            repository.findActiveByUserIdAndTypeForUpdate(
                userId = targetUserId,
                type = DomainSanctionType.BAN,
            ),
        ).thenReturn(staleSanction)
        whenever(repository.save(any())).thenAnswer { invocation ->
            invocation.arguments[0] as Sanction
        }

        val result =
            service.execute(
                CreateSanctionCommand(
                    userId = targetUserId,
                    type = DomainSanctionType.BAN,
                    reason = "Replacement sanction",
                    internalNote = null,
                    duration = Duration.ofDays(1),
                ),
            )

        assertEquals(DomainSanctionStatus.EXPIRED, staleSanction.status)
        assertEquals(DomainSanctionStatus.ACTIVE, result.sanction.status)
        assertEquals(2, publisher.events.size)
    }

    private class RecordingIntegrationEventPublisher : IntegrationEventPublisher {
        val events = mutableListOf<IntegrationEvent<*>>()

        override fun <T : Any> publish(
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            events += event
            return UUID.fromString("0198a000-0000-7000-8000-000000000003")
        }
    }
}
