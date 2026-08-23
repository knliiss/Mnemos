package com.mnemos.wallet.application.balance.usecase.initialize

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.balance.repository.BalanceRepository
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant

class InitializeWalletBalancesServiceTest {
    @Test
    fun `user wallet initializes prime balance`() {
        val balanceRepository = mock<BalanceRepository>()
        val userId = UserId.generate()
        val owner = WalletOwner.User(userId)
        val occurredAt = Instant.parse("2026-08-22T14:17:33Z")
        val service = InitializeWalletBalancesService(balanceRepository)

        service.execute(
            owner = owner,
            occurredAt = occurredAt,
        )

        val balanceCaptor = argumentCaptor<Balance>()
        verify(balanceRepository).createIfAbsent(balanceCaptor.capture())

        val balance = balanceCaptor.firstValue

        assertEquals(owner, balance.owner)
        assertEquals(Currency.PRIME, balance.currency)
        assertEquals(occurredAt, balance.createdAt)
    }

    @Test
    fun `account wallet initializes coin balance`() {
        val balanceRepository = mock<BalanceRepository>()
        val accountId = AccountId.generate()
        val owner = WalletOwner.Account(accountId)
        val occurredAt = Instant.parse("2026-08-22T14:17:33Z")
        val service = InitializeWalletBalancesService(balanceRepository)

        service.execute(
            owner = owner,
            occurredAt = occurredAt,
        )

        val balanceCaptor = argumentCaptor<Balance>()
        verify(balanceRepository).createIfAbsent(balanceCaptor.capture())

        val balance = balanceCaptor.firstValue

        assertEquals(owner, balance.owner)
        assertEquals(Currency.COIN, balance.currency)
        assertEquals(occurredAt, balance.createdAt)
    }
}
