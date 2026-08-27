package com.mnemos.wallet.application.exchange

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.application.account.port.out.ActiveAccountProvider
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionRequest
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionResponse
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionResult
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionService
import com.mnemos.wallet.configuration.CoinPurchaseProperties
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class PurchaseCoinServiceTest {
    private val currentUserProvider = mock<CurrentUserProvider>()
    private val activeAccountProvider = mock<ActiveAccountProvider>()
    private val createTransactionService = mock<CreateTransactionService>()
    private val properties =
        CoinPurchaseProperties().apply {
            coinPerPrime = 100
            primeAmounts = listOf(5, 10, 25, 50)
        }
    private val service =
        PurchaseCoinService(
            currentUserProvider = currentUserProvider,
            activeAccountProvider = activeAccountProvider,
            createTransactionService = createTransactionService,
            properties = properties,
        )

    @Test
    fun `purchases account coins by atomically debiting user prime and crediting account coins`() {
        val userId = UserId(UUID.fromString("019c1129-ef54-7000-8000-000000000901"))
        val accountId = AccountId(UUID.fromString("019c1129-ef54-7000-8000-000000000902"))
        val requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000903")
        val completed = CreateTransactionResult.Completed(mock<CreateTransactionResponse>())

        whenever(currentUserProvider.requireUserId()).thenReturn(userId)
        whenever(activeAccountProvider.findActiveAccountId(userId)).thenReturn(accountId)
        whenever(createTransactionService.create(any(), any(), any())).thenReturn(completed)

        val result =
            service.purchase(
                accountId = accountId,
                requestId = requestId,
                primeAmount = 10,
            )

        val requests = argumentCaptor<CreateTransactionRequest>()
        verify(createTransactionService, org.mockito.kotlin.times(2)).create(requests.capture(), any(), any())

        val primeDebit = requests.allValues[0]
        val coinCredit = requests.allValues[1]

        assertEquals(10, result.primeAmount)
        assertEquals(1_000, result.coinAmount)

        assertEquals(WalletOwner.User(userId), primeDebit.owner)
        assertEquals(Currency.PRIME, primeDebit.currency)
        assertEquals(TransactionType.DEBIT, primeDebit.type)
        assertEquals(TransactionSource.COIN_PURCHASE, primeDebit.source)
        assertEquals(requestId, primeDebit.sourceReferenceId.id)
        assertEquals(10, primeDebit.amount)

        assertEquals(WalletOwner.Account(accountId), coinCredit.owner)
        assertEquals(Currency.COIN, coinCredit.currency)
        assertEquals(TransactionType.CREDIT, coinCredit.type)
        assertEquals(TransactionSource.COIN_PURCHASE, coinCredit.source)
        assertEquals(requestId, coinCredit.sourceReferenceId.id)
        assertEquals(1_000, coinCredit.amount)
    }
}
