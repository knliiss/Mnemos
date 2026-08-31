package com.mnemos.telegram.bot.application.account.port.out

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountMutationResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import java.util.UUID

interface AccountGateway {
    fun getContext(telegramId: Long): TelegramAccountContextResult

    fun createAccount(
        telegramId: Long,
        cristalixNickname: String,
    ): TelegramAccountMutationResult

    fun joinAccount(
        telegramId: Long,
        inviteCode: String,
    ): TelegramAccountMutationResult

    fun changeActiveAccount(
        telegramId: Long,
        accountId: UUID,
    ): TelegramAccountSwitchResult
}
