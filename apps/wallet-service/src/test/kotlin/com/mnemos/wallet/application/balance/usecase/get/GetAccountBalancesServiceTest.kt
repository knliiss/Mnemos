package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.wallet.application.account.port.out.ActiveAccountProvider
import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.application.balance.port.out.BalanceReader
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class GetAccountBalancesServiceTest {
    @Test
    fun `active account balance is readable without account permission`() {
        val accountId = AccountId.generate()
        val userId = UserId.generate()
        val balanceReader = mock<BalanceReader>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val activeAccountProvider = mock<ActiveAccountProvider>()
        val expected = BalanceView(currency = Currency.COIN, amount = 120)

        whenever(currentUserProvider.requireUserId()).thenReturn(userId)
        whenever(activeAccountProvider.findActiveAccountId(userId)).thenReturn(accountId)
        whenever(balanceReader.findByOwner(WalletOwner.Account(accountId))).thenReturn(listOf(expected))

        val service =
            GetAccountBalancesService(
                balanceReader = balanceReader,
                currentUserProvider = currentUserProvider,
                activeAccountProvider = activeAccountProvider,
            )

        val result = service.execute(accountId)

        assertEquals(listOf(expected), result)
        verify(balanceReader).findByOwner(WalletOwner.Account(accountId))
    }

    @Test
    fun `non active account balance is rejected before wallet read`() {
        val requestedAccountId = AccountId.generate()
        val activeAccountId = AccountId.generate()
        val userId = UserId.generate()
        val balanceReader = mock<BalanceReader>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val activeAccountProvider = mock<ActiveAccountProvider>()

        whenever(currentUserProvider.requireUserId()).thenReturn(userId)
        whenever(activeAccountProvider.findActiveAccountId(userId)).thenReturn(activeAccountId)

        val service =
            GetAccountBalancesService(
                balanceReader = balanceReader,
                currentUserProvider = currentUserProvider,
                activeAccountProvider = activeAccountProvider,
            )

        assertThrows(AccountPermissionDeniedException::class.java) {
            service.execute(requestedAccountId)
        }

        verifyNoInteractions(balanceReader)
    }
}
