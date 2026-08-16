package com.mnemos.personalization.application.ownership

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.exception.ActiveAccountRequiredException
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.ownership.port.out.OwnedPersonalizationReader
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.ownership.usecase.listmine.ListOwnedPersonalizationsQuery
import com.mnemos.personalization.application.ownership.usecase.listmine.ListOwnedPersonalizationsService
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ListOwnedPersonalizationsServiceTest {
    @Test
    fun `list mine delegates category and page with readable active account`() {
        val userId = UserId.generate()
        val accountId = AccountId.generate()
        val actorProvider = mock<CurrentPersonalizationActorProvider>()
        val accountAccess = mock<PersonalizationAccountAccess>()
        val ownershipReader = mock<OwnedPersonalizationReader>()
        val page =
            PageQuery(
                page = 1,
                size = 25,
            )
        val query =
            ListOwnedPersonalizationsQuery(
                type = PersonalizationType.NICK_STYLE,
                page = page,
            )
        val expected =
            PageResult<OwnedPersonalization>(
                items = emptyList(),
                page = 1,
                size = 25,
                totalItems = 31,
                totalPages = 2,
                hasNextPage = false,
            )

        whenever(actorProvider.requireActor()).thenReturn(
            PersonalizationActor(
                userId = userId,
                roles = emptySet(),
            ),
        )
        whenever(accountAccess.requireActiveAccount(userId)).thenReturn(accountId)
        whenever(
            accountAccess.hasPermission(
                accountId = accountId,
                userId = userId,
                permission = RolePermission.VIEW_PERSONALIZATIONS,
            ),
        ).thenReturn(true)
        whenever(
            ownershipReader.findOwned(
                userId = userId,
                accountId = accountId,
                type = PersonalizationType.NICK_STYLE,
                page = page,
            ),
        ).thenReturn(expected)

        val service =
            createService(
                actorProvider = actorProvider,
                accountAccess = accountAccess,
                ownershipReader = ownershipReader,
            )

        val result = service.execute(query)

        assertEquals(expected, result)
        verify(ownershipReader).findOwned(
            userId = userId,
            accountId = accountId,
            type = PersonalizationType.NICK_STYLE,
            page = page,
        )
    }

    @Test
    fun `list mine stays user scoped when there is no active account`() {
        val userId = UserId.generate()
        val actorProvider = mock<CurrentPersonalizationActorProvider>()
        val accountAccess = mock<PersonalizationAccountAccess>()
        val ownershipReader = mock<OwnedPersonalizationReader>()
        val page =
            PageQuery(
                page = 0,
                size = 20,
            )
        val query =
            ListOwnedPersonalizationsQuery(
                type = null,
                page = page,
            )
        val expected =
            PageResult<OwnedPersonalization>(
                items = emptyList(),
                page = 0,
                size = 20,
                totalItems = 0,
                totalPages = 0,
                hasNextPage = false,
            )

        whenever(actorProvider.requireActor()).thenReturn(
            PersonalizationActor(
                userId = userId,
                roles = emptySet(),
            ),
        )
        whenever(accountAccess.requireActiveAccount(userId))
            .thenThrow(ActiveAccountRequiredException())
        whenever(
            ownershipReader.findOwned(
                userId = userId,
                accountId = null,
                type = null,
                page = page,
            ),
        ).thenReturn(expected)

        val service =
            createService(
                actorProvider = actorProvider,
                accountAccess = accountAccess,
                ownershipReader = ownershipReader,
            )

        val result = service.execute(query)

        assertEquals(expected, result)
        verify(ownershipReader).findOwned(
            userId = userId,
            accountId = null,
            type = null,
            page = page,
        )
    }

    private fun createService(
        actorProvider: CurrentPersonalizationActorProvider,
        accountAccess: PersonalizationAccountAccess,
        ownershipReader: OwnedPersonalizationReader,
    ): ListOwnedPersonalizationsService {
        val support =
            PersonalizationOwnershipSupport(
                accountAccess = accountAccess,
                personalizationRepository = mock<PersonalizationRepository>(),
                ownershipRepository = mock<OwnedPersonalizationRepository>(),
                purchaseOperationRepository = mock<PurchaseOperationRepository>(),
            )

        return ListOwnedPersonalizationsService(
            actorProvider = actorProvider,
            reader = ownershipReader,
            support = support,
        )
    }
}
