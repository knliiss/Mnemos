package com.mnemos.account.application.context.account

import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.AccountUnavailableException
import com.mnemos.account.domain.exception.account.ActiveAccountNotSetException
import com.mnemos.account.domain.exception.account.member.ActiveAccountMembershipNotFoundException
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import org.springframework.stereotype.Component

@Component
class InternalTelegramAccountContextProvider(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
) {
    fun requirePermission(
        telegramId: TelegramId,
        permission: RolePermission,
    ): ActiveAccountContext? {
        val context =
            createContext(
                telegramId = telegramId,
                lockAccount = false,
            ) ?: return null

        requirePermission(context, permission)

        return context
    }

    fun requirePermissionForUpdate(
        telegramId: TelegramId,
        permission: RolePermission,
    ): ActiveAccountContext? {
        val context =
            createContext(
                telegramId = telegramId,
                lockAccount = true,
            ) ?: return null

        requirePermission(context, permission)

        return context
    }

    private fun createContext(
        telegramId: TelegramId,
        lockAccount: Boolean,
    ): ActiveAccountContext? {
        val user = userRepository.findByTelegramId(telegramId) ?: return null
        val accountId = user.activeAccountId ?: throw ActiveAccountNotSetException(user.id)
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
