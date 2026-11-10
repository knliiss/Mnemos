package com.mnemos.telegram.bot.application.widget.wallet

sealed interface WalletTransactionResult {
    data class Available(
        val data: WalletTransactionPage,
    ) : WalletTransactionResult

    data object AccountRequired : WalletTransactionResult

    data object Forbidden : WalletTransactionResult

    data object Unavailable : WalletTransactionResult
}
