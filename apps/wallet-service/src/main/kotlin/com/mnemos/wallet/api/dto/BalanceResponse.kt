package com.mnemos.wallet.api.dto

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.model.balance.Balance

data class BalanceResponse(
    val currency: Currency,
    val amount: Long,
)

fun Balance.toBalanceResponse(): BalanceResponse =
    BalanceResponse(
        currency = currency,
        amount = amount.value,
    )
