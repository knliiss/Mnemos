package com.mnemos.account.application.service.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class InternalAccountQueryService(
    private val accountRepository: AccountRepository,
) {
    fun findActiveByAccountId(accountId: UUID): Account? =
        accountRepository.findActiveById(AccountId(accountId))

    fun findActiveByUserId(userId: UUID): Account? =
        accountRepository.findActiveByUserId(UserId(userId))

    fun findActiveByCristalixNickname(nickname: String): Account? =
        accountRepository.findActiveByCristalixNickname(nickname)

    fun findBootstrapBatch(
        afterAccountId: UUID?,
        limit: Int,
    ): List<Account> {
        require(limit in 1..MAX_BOOTSTRAP_BATCH_SIZE) {
            "Account bootstrap limit must be between 1 and $MAX_BOOTSTRAP_BATCH_SIZE"
        }

        return accountRepository.findBootstrapBatch(
            afterAccountId = afterAccountId?.let(::AccountId),
            limit = limit,
        )
    }

    private companion object {
        const val MAX_BOOTSTRAP_BATCH_SIZE = 500
    }
}
