package com.mnemos.telegram.bot.application.widget.wallet.port.out

import com.mnemos.telegram.bot.application.widget.wallet.WalletTransactionResult

fun interface WalletTransactionGateway {
    fun list(
        telegramId: Long,
        page: Int,
    ): WalletTransactionResult
}
