package com.mnemos.telegram.bot.application.account.port.out

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import java.util.UUID

interface AccountBalanceGateway {
    fun getBalances(accountId: UUID): TelegramAccountBalanceResult
}
