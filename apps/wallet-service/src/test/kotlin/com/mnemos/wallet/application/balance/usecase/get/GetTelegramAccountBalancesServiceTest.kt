package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.account.port.out.TelegramActiveAccountProvider
import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class GetTelegramAccountBalancesServiceTest {
    @Test
    fun `service reads balances only for account service resolved active account`() {
        val telegramId = 931458258L
        val accountId = AccountId.generate()
        val activeAccountProvider = mock<TelegramActiveAccountProvider>()
        val getWalletBalancesService = mock<GetWalletBalancesService>()
        val balances =
            listOf(
                BalanceView(
                    currency = Currency.COIN,
                    amount = 125,
                ),
                BalanceView(
                    currency = Currency.PRIME,
                    amount = 3,
                ),
            )

        whenever(activeAccountProvider.findActiveAccountId(telegramId)).thenReturn(accountId)
        whenever(
            getWalletBalancesService.execute(
                ownerType = WalletOwnerType.ACCOUNT,
                ownerId = accountId.id,
            ),
        ).thenReturn(balances)

        val service =
            GetTelegramAccountBalancesService(
                telegramActiveAccountProvider = activeAccountProvider,
                getWalletBalancesService = getWalletBalancesService,
            )

        val result = service.execute(telegramId)

        assertEquals(balances, result)
        verify(getWalletBalancesService).execute(
            ownerType = WalletOwnerType.ACCOUNT,
            ownerId = accountId.id,
        )
    }

    @Test
    fun `service does not read arbitrary wallet when Telegram user has no active account`() {
        val telegramId = 931458258L
        val activeAccountProvider = mock<TelegramActiveAccountProvider>()
        val getWalletBalancesService = mock<GetWalletBalancesService>()

        whenever(activeAccountProvider.findActiveAccountId(telegramId)).thenReturn(null)

        val service =
            GetTelegramAccountBalancesService(
                telegramActiveAccountProvider = activeAccountProvider,
                getWalletBalancesService = getWalletBalancesService,
            )

        val result = service.execute(telegramId)

        assertNull(result)
        verifyNoInteractions(getWalletBalancesService)
    }
}
