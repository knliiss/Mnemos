package com.mnemos.telegram.bot.application.account.port.out

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import java.util.UUID

interface AccountGateway {
    fun getContext(telegramId: Long): TelegramAccountContextResult

    fun changeActiveAccount(
        telegramId: Long,
        accountId: UUID,
    ): TelegramAccountSwitchResult
}
