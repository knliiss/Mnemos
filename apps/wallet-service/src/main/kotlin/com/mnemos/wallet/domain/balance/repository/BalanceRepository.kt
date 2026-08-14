package com.mnemos.wallet.domain.balance.repository

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.wallet.model.WalletOwner

interface BalanceRepository {
    fun findForUpdate(
        owner: WalletOwner,
        currency: Currency,
    ): Balance?

    fun createIfAbsent(balance: Balance): Boolean

    fun update(balance: Balance)
}
