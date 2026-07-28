package com.mnemos.account.application.context.account

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.domain.exception.account.AccountNotActiveException
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.AccountUnavailableException
import com.mnemos.account.domain.exception.account.member.ActiveAccountMembershipNotFoundException
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import org.springframework.stereotype.Component

@Component
class ActiveAccountContextProvider(
    private val currentUserProvider: CurrentUserProvider,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
) {
    fun requireActiveContext(accountId: AccountId): ActiveAccountContext =
        createContext(
            accountId = accountId,
            lockAccount = false,
        )

    fun requireActiveContextForUpdate(accountId: AccountId): ActiveAccountContext =
        createContext(
            accountId = accountId,
            lockAccount = true,
        )

    fun requirePermission(
        accountId: AccountId,
        permission: RolePermission,
    ): ActiveAccountContext {
        val context = requireActiveContext(accountId)

        requirePermission(context, permission)

        return context
    }

    fun requirePermissionForUpdate(
        accountId: AccountId,
        permission: RolePermission,
    ): ActiveAccountContext {
        val context = requireActiveContextForUpdate(accountId)

        requirePermission(context, permission)

        return context
    }

    private fun createContext(
        accountId: AccountId,
        lockAccount: Boolean,
    ): ActiveAccountContext {
        val user = currentUserProvider.get()

        if (user.activeAccountId != accountId) {
            throw AccountNotActiveException(accountId)
        }

        val account =
            if (lockAccount) {
                accountRepository.findByIdForUpdate(accountId)
            } else {
                accountRepository.findByIdIncludingInactive(accountId)
            } ?: throw AccountNotFoundException(accountId)

        if (account.status != AccountStatus.ACTIVE) {
            throw AccountUnavailableException(
                accountId = account.id,
                status = account.status,
            )
        }

        val member =
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = accountId,
                userId = user.id,
            ) ?: throw ActiveAccountMembershipNotFoundException(
                accountId = accountId,
                userId = user.id,
            )

        return ActiveAccountContext(
            user = user,
            account = account,
            member = member,
        )
    }

    private fun requirePermission(
        context: ActiveAccountContext,
        permission: RolePermission,
    ) {
        val hasPermission =
            accountMemberRepository.hasEffectivePermission(
                memberId = context.member.id,
                permission = permission,
            )

        if (!hasPermission) {
            throw AccountPermissionDeniedException(
                accountId = context.account.id,
                requiredPermissions = setOf(permission.name),
            )
        }
    }
}
