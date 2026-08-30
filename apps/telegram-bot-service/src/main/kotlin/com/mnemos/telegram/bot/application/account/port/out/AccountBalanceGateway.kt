package com.mnemos.telegram.bot.application.account.port.out

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult

interface AccountBalanceGateway {
    fun getBalances(telegramId: Long): TelegramAccountBalanceResult
}
