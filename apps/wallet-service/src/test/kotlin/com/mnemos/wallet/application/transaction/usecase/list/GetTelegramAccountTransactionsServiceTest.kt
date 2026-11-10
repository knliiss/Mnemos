package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.wallet.application.account.model.TelegramWalletContext
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
import com.mnemos.wallet.application.account.port.out.TelegramWalletContextProvider
import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class GetTelegramAccountTransactionsServiceTest {
    private val contexts = mock<TelegramWalletContextProvider>()
    private val permissions = mock<AccountPermissionProvider>()
    private val transactions = mock<TransactionReader>()
    private val service = GetTelegramAccountTransactionsService(contexts, permissions, transactions)
    private val accountId = AccountId.generate()
    private val userId = UserId.generate()
    private val page = PageQuery(page = 0, size = 4)

    @Test
    fun `returns no transactions without a linked active account`() {
        whenever(contexts.findByTelegramId(11L)).thenReturn(TelegramWalletContext(userId, null))

        assertNull(service.execute(11L, page))
        verifyNoInteractions(permissions, transactions)
    }

    @Test
    fun `enforces history permission before any wallet read`() {
        whenever(contexts.findByTelegramId(11L)).thenReturn(TelegramWalletContext(userId, accountId))
        whenever(permissions.hasPermission(accountId, userId, RolePermission.VIEW_TRANSACTION_HISTORY))
            .thenReturn(false)

        assertThrows(AccountPermissionDeniedException::class.java) {
            service.execute(11L, page)
        }

        verifyNoInteractions(transactions)
    }

    @Test
    fun `reads only active account transactions after permission is granted`() {
        val expected =
            PageResult<TransactionView>(
                items = emptyList(),
                page = 0,
                size = 4,
                totalItems = 0,
                totalPages = 0,
                hasNextPage = false,
            )
        whenever(contexts.findByTelegramId(11L)).thenReturn(TelegramWalletContext(userId, accountId))
        whenever(permissions.hasPermission(accountId, userId, RolePermission.VIEW_TRANSACTION_HISTORY))
            .thenReturn(true)
        whenever(transactions.findByOwner(WalletOwner.Account(accountId), page)).thenReturn(expected)

        assertEquals(TelegramAccountTransactionSnapshot(accountId, expected), service.execute(11L, page))
        verify(permissions).hasPermission(accountId, userId, RolePermission.VIEW_TRANSACTION_HISTORY)
        verify(transactions).findByOwner(WalletOwner.Account(accountId), page)
    }
}
