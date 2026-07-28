package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.application.context.CurrentUserProvider
import com.mnemos.accountservice.application.query.account.AccountDetails
import com.mnemos.accountservice.application.query.account.AccountQueryRepository
import com.mnemos.accountservice.application.query.account.AccountSummary
import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccountQueryService(
    private val currentUserProvider: CurrentUserProvider,
    private val accountQueryRepository: AccountQueryRepository,
) {
    fun getAvailableAccounts(): List<AccountSummary> {
        val user = currentUserProvider.get()

        return accountQueryRepository.findSummariesByUserId(
            userId = user.id,
            activeAccountId = user.activeAccountId,
        )
    }

    fun getCurrentActiveAccount(): AccountDetails? {
        val user = currentUserProvider.get()
        val activeAccountId = user.activeAccountId ?: return null

        return accountQueryRepository.findDetailsByAccountIdAndUserId(
            accountId = activeAccountId,
            userId = user.id,
            activeAccountId = activeAccountId,
        ) ?: throw AccountNotFoundException(activeAccountId)
    }
}
