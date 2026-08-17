package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.domain.reaction.model.AccountReactionType
import jakarta.validation.constraints.NotNull

data class SetAccountReactionRequest(
    @field:NotNull
    val type: AccountReactionType,
)
