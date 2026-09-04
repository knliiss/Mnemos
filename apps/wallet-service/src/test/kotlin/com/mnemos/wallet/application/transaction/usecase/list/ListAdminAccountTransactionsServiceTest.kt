package com.mnemos.wallet.application.transaction.usecase.list

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.common.pagination.PageResult
import com.mnemos.wallet.application.transaction.model.TransactionView
import com.mnemos.wallet.application.transaction.port.out.TransactionReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ListAdminAccountTransactionsServiceTest {
    @Test
    fun `admin transaction history reads the requested account directly`() {
        val accountId = AccountId.generate()
        val page = PageQuery(page = 2, size = 20)
        val expected =
            PageResult<TransactionView>(
                items = emptyList(),
                page = 2,
                size = 20,
                totalItems = 0,
                totalPages = 0,
                hasNextPage = false,
            )
        val transactionReader = mock<TransactionReader>()

        whenever(
            transactionReader.findByOwner(
                owner = WalletOwner.Account(accountId),
                page = page,
            ),
        ).thenReturn(expected)

        val service =
            ListAdminAccountTransactionsService(
                transactionReader = transactionReader,
            )

        val result =
            service.execute(
                accountId = accountId,
                page = page,
            )

        assertEquals(expected, result)
        verify(transactionReader).findByOwner(
            owner = WalletOwner.Account(accountId),
            page = page,
        )
    }
}
