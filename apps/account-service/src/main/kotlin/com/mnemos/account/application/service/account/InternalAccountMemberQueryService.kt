package com.mnemos.account.application.service.account

import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class InternalAccountMemberQueryService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
) {
    fun findUserIds(accountId: AccountId): List<UserId>? {
        if (accountRepository.findByIdIncludingInactive(accountId) == null) {
            return null
        }

        return accountMemberRepository.findUserIdsByAccountId(accountId)
    }
}
