package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.application.query.account.AccountContext
import com.mnemos.account.application.query.account.AccountDetails
import com.mnemos.account.application.query.account.AccountQueryRepository
import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccountQueryService(
    private val currentUserProvider: CurrentUserProvider,
    private val accountQueryRepository: AccountQueryRepository,
    private val accountMemberRepository: AccountMemberRepository,
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

    fun getCurrentContext(): AccountContext {
        val user = currentUserProvider.get()
        val availableAccounts =
            accountQueryRepository.findSummariesByUserId(
                userId = user.id,
                activeAccountId = user.activeAccountId,
            )
        val activeAccountId = user.activeAccountId

        if (activeAccountId == null) {
            return AccountContext(
                availableAccounts = availableAccounts,
                activeAccount = null,
                effectivePermissions = emptySet(),
            )
        }

        val activeAccount =
            accountQueryRepository.findDetailsByAccountIdAndUserId(
                accountId = activeAccountId,
                userId = user.id,
                activeAccountId = activeAccountId,
            ) ?: throw AccountNotFoundException(activeAccountId)

        val effectivePermissions =
            if (activeAccount.owner) {
                RolePermission.entries.toSet()
            } else {
                accountMemberRepository.findPermissions(activeAccount.currentMemberId)
            }

        return AccountContext(
            availableAccounts = availableAccounts,
            activeAccount = activeAccount,
            effectivePermissions = effectivePermissions,
        )
    }
}
