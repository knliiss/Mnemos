package com.mnemos.social.application.accountreaction.usecase.summary

import com.mnemos.social.application.accountreaction.model.AccountReactionSummary
import java.util.UUID

interface GetAccountReactionSummaryUseCase {
    fun execute(targetAccountId: UUID): AccountReactionSummary
}
