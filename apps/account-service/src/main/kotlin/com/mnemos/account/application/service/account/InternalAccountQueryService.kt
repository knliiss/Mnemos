package com.mnemos.account.application.service.account

import com.mnemos.account.application.query.account.InternalAccountBootstrapQuery
import com.mnemos.account.application.query.account.ResolveInternalAccountQuery
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.repository.account.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class InternalAccountQueryService(
    private val accountRepository: AccountRepository,
) {
    fun resolve(query: ResolveInternalAccountQuery): Account? =
        when (query) {
            is ResolveInternalAccountQuery.ByAccountId -> accountRepository.findActiveById(query.accountId)
            is ResolveInternalAccountQuery.ByUserId -> accountRepository.findActiveByUserId(query.userId)
            is ResolveInternalAccountQuery.ByCristalixNickname ->
                accountRepository.findActiveByCristalixNickname(query.cristalixNickname)
        }

    fun findBootstrapBatch(query: InternalAccountBootstrapQuery): List<Account> {
        require(query.limit in 1..MAX_BOOTSTRAP_BATCH_SIZE) {
            "Account bootstrap limit must be between 1 and $MAX_BOOTSTRAP_BATCH_SIZE"
        }

        return accountRepository.findBootstrapBatch(
            afterAccountId = query.afterAccountId,
            limit = query.limit,
        )
    }

    private companion object {
        const val MAX_BOOTSTRAP_BATCH_SIZE = 500
    }
}
