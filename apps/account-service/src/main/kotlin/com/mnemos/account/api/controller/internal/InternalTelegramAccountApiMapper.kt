package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.application.query.account.InternalTelegramAccountContext
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountContextResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountStatus
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountSummaryResponse

object InternalTelegramAccountApiMapper {
    fun InternalTelegramAccountContext.toResponse(): InternalTelegramAccountContextResponse =
        InternalTelegramAccountContextResponse(
            activeAccountId = activeAccountId?.id,
            accounts = accounts.map { account -> account.toResponse() },
        )

    private fun AccountSummary.toResponse(): InternalTelegramAccountSummaryResponse =
        InternalTelegramAccountSummaryResponse(
            accountId = id.id,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            memberCount = memberCount,
            maxMembers = maxMembers,
            owner = owner,
            active = active,
            status = InternalTelegramAccountStatus.valueOf(status.name),
        )
}
