package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.domain.reaction.model.AccountReactionType

data class AccountReactionStateResponse(
    val type: AccountReactionType?,
)
