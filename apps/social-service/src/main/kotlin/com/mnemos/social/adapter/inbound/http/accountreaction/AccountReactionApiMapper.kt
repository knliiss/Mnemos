package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.application.accountreaction.model.AccountReactionSummary
import com.mnemos.social.application.accountreaction.usecase.set.SetAccountReactionCommand
import java.util.UUID

object AccountReactionApiMapper {
    fun SetAccountReactionRequest.toCommand(accountId: UUID): SetAccountReactionCommand =
        SetAccountReactionCommand(
            targetAccountId = accountId,
            type = type,
        )

    fun AccountReactionSummary.toResponse(): AccountReactionSummaryResponse =
        AccountReactionSummaryResponse(
            likes = likes,
            dislikes = dislikes,
        )
}
