package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.balance.model.BalanceView

fun interface GetAccountBalancesUseCase {
    fun execute(accountId: AccountId): List<BalanceView>
}
