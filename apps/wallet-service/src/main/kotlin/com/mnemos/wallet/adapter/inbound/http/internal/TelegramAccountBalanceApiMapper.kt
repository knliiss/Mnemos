package com.mnemos.wallet.adapter.inbound.http.internal

import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountBalanceResponse

object TelegramAccountBalanceApiMapper {
    fun BalanceView.toTelegramResponse(): TelegramAccountBalanceResponse =
        TelegramAccountBalanceResponse(
            currency = currency,
            amount = amount,
        )
}
