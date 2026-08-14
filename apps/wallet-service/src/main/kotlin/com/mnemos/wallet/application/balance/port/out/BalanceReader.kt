package com.mnemos.wallet.application.balance.port.out

import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.domain.wallet.model.WalletOwner

fun interface BalanceReader {
    fun findByOwner(owner: WalletOwner): List<BalanceView>
}
