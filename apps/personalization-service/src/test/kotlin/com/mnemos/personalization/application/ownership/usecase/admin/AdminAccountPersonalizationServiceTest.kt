package com.mnemos.personalization.application.ownership.usecase.admin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AdminAccountPersonalizationServiceTest {
    private val now = Instant.parse("2026-09-03T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `list reads ownerships for requested account`() {
        val fixture = fixture()
        val accountId = AccountId.generate()
        val ownership = accountOwnership(accountId)

        whenever(fixture.ownershipRepository.findAll(PersonalizationOwner.Account(accountId))).thenReturn(listOf(ownership))

        val result = fixture.service.list(accountId)

        assertSame(ownership, result.single())
        verify(fixture.ownershipRepository).findAll(PersonalizationOwner.Account(accountId))
        verifyNoInteractions(fixture.actorProvider)
    }

    @Test
    fun `grant rejects user personalization for account`() {
        val fixture = fixture()
        val accountId = AccountId.generate()
        val personalizationId = PersonalizationId.generate()
        val personalization = mock<Personalization>()

        whenever(personalization.id).thenReturn(personalizationId)
        whenever(personalization.type).thenReturn(PersonalizationType.NOTIFICATION_PACK)
        whenever(fixture.ownershipSupport.requirePersonalizationForUpdate(personalizationId)).thenReturn(personalization)

        assertThrows(PersonalizationAccessDeniedException::class.java) {
            fixture.service.grant(accountId, personalizationId)
        }

        verify(fixture.ownershipRepository, never()).save(any())
        verifyNoInteractions(fixture.actorProvider)
    }

    @Test
    fun `activate replaces active personalization of same type and publishes appearance change`() {
        val fixture = fixture()
        val accountId = AccountId.generate()
        val ownership = accountOwnership(accountId)
        val owner = PersonalizationOwner.Account(accountId)

        whenever(fixture.ownershipRepository.findForUpdate(owner, ownership.personalizationId)).thenReturn(ownership)
        whenever(fixture.ownershipSupport.saveOwnership(ownership)).thenReturn(ownership)

        val result = fixture.service.activate(accountId, ownership.personalizationId)

        assertSame(ownership, result)
        assertTrue(result.active)
        verify(fixture.ownershipRepository).deactivateActive(
            owner = owner,
            type = ownership.type,
        )
        verify(fixture.profileAppearanceEventPublisher).publish(
            accountId = accountId,
            actorUserId = fixture.actorUserId,
        )
    }

    @Test
    fun `deactivate inactive personalization is idempotent without appearance event`() {
        val fixture = fixture()
        val accountId = AccountId.generate()
        val ownership = accountOwnership(accountId)
        val owner = PersonalizationOwner.Account(accountId)

        whenever(fixture.ownershipRepository.findForUpdate(owner, ownership.personalizationId)).thenReturn(ownership)

        val result = fixture.service.deactivate(accountId, ownership.personalizationId)

        assertSame(ownership, result)
        assertFalse(result.active)
        verify(fixture.ownershipSupport, never()).saveOwnership(any())
        verifyNoInteractions(fixture.profileAppearanceEventPublisher)
        verifyNoInteractions(fixture.actorProvider)
    }

    @Test
    fun `revoke active personalization removes ownership and publishes appearance change`() {
        val fixture = fixture()
        val accountId = AccountId.generate()
        val ownership = accountOwnership(accountId, active = true)
        val owner = PersonalizationOwner.Account(accountId)

        whenever(fixture.ownershipRepository.findForUpdate(owner, ownership.personalizationId)).thenReturn(ownership)

        fixture.service.revoke(accountId, ownership.personalizationId)

        verify(fixture.ownershipRepository).delete(
            owner = owner,
            personalizationId = ownership.personalizationId,
        )
        verify(fixture.profileAppearanceEventPublisher).publish(
            accountId = accountId,
            actorUserId = fixture.actorUserId,
        )
    }

    @Test
    fun `missing ownership cannot be activated`() {
        val fixture = fixture()
        val accountId = AccountId.generate()
        val personalizationId = PersonalizationId.generate()

        whenever(
            fixture.ownershipRepository.findForUpdate(
                PersonalizationOwner.Account(accountId),
                personalizationId,
            ),
        ).thenReturn(null)

        assertThrows(PersonalizationNotOwnedException::class.java) {
            fixture.service.activate(accountId, personalizationId)
        }

        verifyNoInteractions(fixture.profileAppearanceEventPublisher)
        verifyNoInteractions(fixture.actorProvider)
    }

    private fun fixture(): Fixture {
        val ownershipRepository = mock<OwnedPersonalizationRepository>()
        val ownershipSupport = mock<PersonalizationOwnershipSupport>()
        val actorProvider = mock<CurrentPersonalizationActorProvider>()
        val profileAppearanceEventPublisher = mock<ProfileAppearanceEventPublisher>()
        val actorUserId = UserId.generate()

        whenever(actorProvider.requireActor()).thenReturn(
            PersonalizationActor(
                userId = actorUserId,
                roles = emptySet(),
            ),
        )

        val service =
            AdminAccountPersonalizationService(
                ownershipRepository = ownershipRepository,
                ownershipSupport = ownershipSupport,
                actorProvider = actorProvider,
                profileAppearanceEventPublisher = profileAppearanceEventPublisher,
                clock = clock,
            )

        return Fixture(
            service = service,
            ownershipRepository = ownershipRepository,
            ownershipSupport = ownershipSupport,
            actorProvider = actorProvider,
            profileAppearanceEventPublisher = profileAppearanceEventPublisher,
            actorUserId = actorUserId,
        )
    }

    private fun accountOwnership(
        accountId: AccountId,
        active: Boolean = false,
    ): OwnedPersonalization =
        OwnedPersonalization(
            owner = PersonalizationOwner.Account(accountId),
            personalizationId = PersonalizationId.generate(),
            type = PersonalizationType.NICK_STYLE,
            acquiredAt = now,
            active = active,
        )

    private data class Fixture(
        val service: AdminAccountPersonalizationService,
        val ownershipRepository: OwnedPersonalizationRepository,
        val ownershipSupport: PersonalizationOwnershipSupport,
        val actorProvider: CurrentPersonalizationActorProvider,
        val profileAppearanceEventPublisher: ProfileAppearanceEventPublisher,
        val actorUserId: UserId,
    )
}
