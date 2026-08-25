package com.mnemos.account.application.service.account

import com.mnemos.account.application.query.account.AccountLookupSummary
import com.mnemos.account.application.query.account.AccountQueryRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetInternalAccountSummariesService(
    private val accountQueryRepository: AccountQueryRepository,
) {
    fun execute(accountIds: Set<UUID>): List<AccountLookupSummary> {
        require(accountIds.size <= MAX_BATCH_SIZE) {
            "Account summary batch cannot exceed $MAX_BATCH_SIZE accounts"
        }

        return accountQueryRepository.findLookupSummariesByAccountIds(
            accountIds.mapTo(linkedSetOf(), ::AccountId),
        )
    }

    private companion object {
        const val MAX_BATCH_SIZE = 100
    }
}
