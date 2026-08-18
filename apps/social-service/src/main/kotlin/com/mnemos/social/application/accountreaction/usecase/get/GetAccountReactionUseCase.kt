package com.mnemos.social.application.accountreaction.usecase.get

import com.mnemos.social.domain.reaction.model.AccountReactionType
import java.util.UUID

interface GetAccountReactionUseCase {
    fun execute(targetAccountId: UUID): AccountReactionType?
}
