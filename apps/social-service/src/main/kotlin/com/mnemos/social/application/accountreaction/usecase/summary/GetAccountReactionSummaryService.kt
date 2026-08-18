package com.mnemos.social.application.accountreaction.usecase.summary

import com.mnemos.social.application.accountreaction.model.AccountReactionSummary
import com.mnemos.social.domain.reaction.model.AccountReactionType
import com.mnemos.social.domain.reaction.repository.AccountReactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetAccountReactionSummaryService(
    private val repository: AccountReactionRepository,
) : GetAccountReactionSummaryUseCase {
    @Transactional(readOnly = true)
    override fun execute(targetAccountId: UUID): AccountReactionSummary =
        AccountReactionSummary(
            likes =
                repository.countByTargetAndType(
                    targetAccountId = targetAccountId,
                    type = AccountReactionType.LIKE,
                ),
            dislikes =
                repository.countByTargetAndType(
                    targetAccountId = targetAccountId,
                    type = AccountReactionType.DISLIKE,
                ),
        )
}
