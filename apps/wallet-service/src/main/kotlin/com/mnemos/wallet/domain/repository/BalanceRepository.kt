package com.mnemos.wallet.domain.repository

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.api.dto.BalanceResponse
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.model.balance.Balance

interface BalanceRepository {
    fun findByAccountIdAndCurrencyForUpdate(
        accountId: AccountId,
        currency: Currency,
    ): Balance?

    fun update(balance: Balance)

    fun createIfAbsent(balance: Balance): Boolean

    fun findAllByAccountId(accountId: AccountId): List<BalanceResponse>
}
