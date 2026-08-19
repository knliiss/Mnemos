package com.mnemos.social.application.accountreaction.usecase.set

import com.mnemos.social.domain.reaction.model.AccountReactionType
import java.util.UUID

data class SetAccountReactionCommand(
    val targetAccountId: UUID,
    val type: AccountReactionType,
)
