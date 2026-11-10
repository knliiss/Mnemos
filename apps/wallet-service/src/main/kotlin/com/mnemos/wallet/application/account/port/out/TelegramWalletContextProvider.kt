package com.mnemos.wallet.application.account.port.out

import com.mnemos.wallet.application.account.model.TelegramWalletContext

fun interface TelegramWalletContextProvider {
    fun findByTelegramId(telegramId: Long): TelegramWalletContext?
}
