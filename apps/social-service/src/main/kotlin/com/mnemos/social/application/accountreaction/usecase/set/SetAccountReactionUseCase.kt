package com.mnemos.social.application.accountreaction.usecase.set

import com.mnemos.social.domain.reaction.model.AccountReactionType
import java.util.UUID

interface SetAccountReactionUseCase {
    fun execute(
        targetAccountId: UUID,
        type: AccountReactionType,
    )
}
