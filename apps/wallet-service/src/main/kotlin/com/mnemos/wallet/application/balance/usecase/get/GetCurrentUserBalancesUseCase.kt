package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.wallet.application.balance.model.BalanceView

fun interface GetCurrentUserBalancesUseCase {
    fun execute(): List<BalanceView>
}
