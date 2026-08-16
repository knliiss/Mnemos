package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.application.reaction.AccountReactionSummary

object AccountReactionApiMapper {
    fun AccountReactionSummary.toResponse(): AccountReactionSummaryResponse =
        AccountReactionSummaryResponse(
            likes = likes,
            dislikes = dislikes,
        )
}
