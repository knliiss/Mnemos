package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
import com.mnemos.wallet.application.account.port.out.ActiveAccountProvider
import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class ListAccountTransactionsServiceTest {
    @Test
    fun `transaction history requires active account and history permission`() {
        val accountId = AccountId.generate()
        val userId = UserId.generate()
        val page = PageQuery(page = 0, size = 20)
        val expected =
            PageResult<TransactionView>(
                items = emptyList(),
                page = 0,
                size = 20,
                totalItems = 0,
                totalPages = 0,
                hasNextPage = false,
            )
        val transactionReader = mock<TransactionReader>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val activeAccountProvider = mock<ActiveAccountProvider>()
        val accountPermissionProvider = mock<AccountPermissionProvider>()

        whenever(currentUserProvider.requireUserId()).thenReturn(userId)
        whenever(activeAccountProvider.findActiveAccountId(userId)).thenReturn(accountId)
        whenever(
            accountPermissionProvider.hasPermission(
                accountId = accountId,
                userId = userId,
                permission = RolePermission.VIEW_TRANSACTION_HISTORY,
            ),
        ).thenReturn(true)
        whenever(
            transactionReader.findByOwner(
                owner = WalletOwner.Account(accountId),
                page = page,
            ),
        ).thenReturn(expected)

        val service =
            ListAccountTransactionsService(
                transactionReader = transactionReader,
                currentUserProvider = currentUserProvider,
                activeAccountProvider = activeAccountProvider,
                accountPermissionProvider = accountPermissionProvider,
            )

        val result =
            service.execute(
                ListAccountTransactionsQuery(
                    accountId = accountId,
                    page = page,
                ),
            )

        assertEquals(expected, result)
        verify(accountPermissionProvider).hasPermission(
            accountId = accountId,
            userId = userId,
            permission = RolePermission.VIEW_TRANSACTION_HISTORY,
        )
        verify(transactionReader).findByOwner(
            owner = WalletOwner.Account(accountId),
            page = page,
        )
    }

    @Test
    fun `transaction history without history permission is rejected before wallet read`() {
        val accountId = AccountId.generate()
        val userId = UserId.generate()
        val transactionReader = mock<TransactionReader>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val activeAccountProvider = mock<ActiveAccountProvider>()
        val accountPermissionProvider = mock<AccountPermissionProvider>()

        whenever(currentUserProvider.requireUserId()).thenReturn(userId)
        whenever(activeAccountProvider.findActiveAccountId(userId)).thenReturn(accountId)
        whenever(
            accountPermissionProvider.hasPermission(
                accountId = accountId,
                userId = userId,
                permission = RolePermission.VIEW_TRANSACTION_HISTORY,
            ),
        ).thenReturn(false)

        val service =
            ListAccountTransactionsService(
                transactionReader = transactionReader,
                currentUserProvider = currentUserProvider,
                activeAccountProvider = activeAccountProvider,
                accountPermissionProvider = accountPermissionProvider,
            )

        assertThrows(AccountPermissionDeniedException::class.java) {
            service.execute(
                ListAccountTransactionsQuery(
                    accountId = accountId,
                    page = PageQuery(page = 0, size = 20),
                ),
            )
        }

        verifyNoInteractions(transactionReader)
    }

    @Test
    fun `transaction history for non active account is rejected before permission check`() {
        val requestedAccountId = AccountId.generate()
        val activeAccountId = AccountId.generate()
        val userId = UserId.generate()
        val transactionReader = mock<TransactionReader>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val activeAccountProvider = mock<ActiveAccountProvider>()
        val accountPermissionProvider = mock<AccountPermissionProvider>()

        whenever(currentUserProvider.requireUserId()).thenReturn(userId)
        whenever(activeAccountProvider.findActiveAccountId(userId)).thenReturn(activeAccountId)

        val service =
            ListAccountTransactionsService(
                transactionReader = transactionReader,
                currentUserProvider = currentUserProvider,
                activeAccountProvider = activeAccountProvider,
                accountPermissionProvider = accountPermissionProvider,
            )

        assertThrows(AccountPermissionDeniedException::class.java) {
            service.execute(
                ListAccountTransactionsQuery(
                    accountId = requestedAccountId,
                    page = PageQuery(page = 0, size = 20),
                ),
            )
        }

        verifyNoInteractions(accountPermissionProvider)
        verifyNoInteractions(transactionReader)
    }
}
