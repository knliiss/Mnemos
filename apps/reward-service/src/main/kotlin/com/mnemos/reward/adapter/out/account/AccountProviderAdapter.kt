package com.mnemos.reward.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.reward.application.daily.port.out.AccountByNicknameProvider
import com.mnemos.reward.application.daily.port.out.ActiveAccountContext
import com.mnemos.reward.application.daily.port.out.ActiveAccountProvider
import com.mnemos.reward.domain.daily.exception.ActiveAccountRequiredException
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class AccountProviderAdapter(
    private val client: AccountInternalClient,
    private val currentUser: CurrentMnemosUser,
) : ActiveAccountProvider,
    AccountByNicknameProvider {
    override fun getActiveAccountContext(): ActiveAccountContext {
        val userId = UserId(currentUser.requireUserId())

        return resolveActiveAccount(userId)
    }

    override fun getActiveAccountContext(permission: RolePermission): ActiveAccountContext {
        val userId = UserId(currentUser.requireUserId())
        val context = resolveActiveAccount(userId)
        val granted =
            client
                .checkPermission(
                    accountId = context.accountId.id,
                    request =
                        CheckAccountPermissionRequest(
                            userId = userId.id,
                            permission = permission,
                        ),
                ).granted

        if (!granted) {
            throw AccountPermissionDeniedException(
                accountId = context.accountId,
                requiredPermissions = setOf(permission.name),
            )
        }

        return context
    }

    override fun findAccountId(nickname: String): AccountId? =
        client
            .resolve(cristalixNickname = nickname)
            ?.accountId
            ?.let(::AccountId)

    private fun resolveActiveAccount(userId: UserId): ActiveAccountContext {
        val accountId =
            client
                .resolve(userId = userId.id)
                ?.accountId
                ?.let(::AccountId)
                ?: throw ActiveAccountRequiredException()

        return ActiveAccountContext(
            accountId = accountId,
            userId = userId,
        )
    }
}
