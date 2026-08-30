package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountContextResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountStatus
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountSummaryResponse
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSummary

object AccountApiMapper {
    fun InternalTelegramAccountContextResponse.toModel(): TelegramAccountContext =
        TelegramAccountContext(
            activeAccountId = activeAccountId,
            accounts = accounts.map { account -> account.toModel() },
        )

    private fun InternalTelegramAccountSummaryResponse.toModel(): TelegramAccountSummary =
        TelegramAccountSummary(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            memberCount = memberCount,
            maxMembers = maxMembers,
            owner = owner,
            active = active,
            status = status.toModel(),
        )

    private fun InternalTelegramAccountStatus.toModel(): TelegramAccountStatus =
        TelegramAccountStatus.valueOf(name)
}
