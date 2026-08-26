package com.mnemos.personalization.application.ownership.usecase.rebuild

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class RebuildProfileAppearanceServiceTest {
    private val actorProvider = mock<CurrentPersonalizationActorProvider>()
    private val ownershipRepository = mock<OwnedPersonalizationRepository>()
    private val profileAppearanceEventPublisher = mock<ProfileAppearanceEventPublisher>()
    private val service =
        RebuildProfileAppearanceService(
            actorProvider = actorProvider,
            ownershipRepository = ownershipRepository,
            profileAppearanceEventPublisher = profileAppearanceEventPublisher,
        )

    @Test
    fun `system reconciliation republishes every active account without user actor`() {
        val firstAccountId = AccountId(UUID.fromString("019c1c70-0000-7000-8000-000000000001"))
        val secondAccountId = AccountId(UUID.fromString("019c1c70-0000-7000-8000-000000000002"))

        whenever(ownershipRepository.findActiveAccountIds(anyAppearanceTypes()))
            .thenReturn(listOf(firstAccountId, secondAccountId))

        val rebuiltAccounts = service.executeSystem()

        assertEquals(2, rebuiltAccounts)
        verify(profileAppearanceEventPublisher).publish(
            accountId = firstAccountId,
            actorUserId = null,
        )
        verify(profileAppearanceEventPublisher).publish(
            accountId = secondAccountId,
            actorUserId = null,
        )
    }

    @Test
    fun `admin rebuild preserves requesting user as event actor`() {
        val accountId = AccountId(UUID.fromString("019c1c70-0000-7000-8000-000000000003"))
        val userId = UserId(UUID.fromString("019c1c70-0000-7000-8000-000000000004"))

        whenever(actorProvider.requireActor())
            .thenReturn(
                PersonalizationActor(
                    userId = userId,
                    roles = emptySet(),
                ),
            )
        whenever(ownershipRepository.findActiveAccountIds(anyAppearanceTypes()))
            .thenReturn(listOf(accountId))

        val rebuiltAccounts = service.execute()

        assertEquals(1, rebuiltAccounts)
        verify(profileAppearanceEventPublisher).publish(
            accountId = accountId,
            actorUserId = userId,
        )
    }

    private fun anyAppearanceTypes() =
        org.mockito.kotlin.any<Set<com.mnemos.personalization.domain.personalization.model.PersonalizationType>>()
}
