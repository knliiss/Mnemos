package com.mnemos.bet.adapter.out.account

import com.mnemos.bet.application.account.model.AccountSnapshot
import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountProviderAdapter(
    private val client: AccountInternalClient,
    private val currentUser: CurrentMnemosUser,
) : AccountProvider {
    override fun requireActiveAccount(): AccountSnapshot {
        val userId = currentUser.requireUserId()

        return resolveActiveAccount(userId)
    }

    override fun requireActiveAccount(permission: RolePermission): AccountSnapshot = requireActiveAccount(setOf(permission))

    override fun requireActiveAccount(permissions: Set<RolePermission>): AccountSnapshot {
        require(permissions.isNotEmpty()) { "At least one account permission is required" }

        val userId = currentUser.requireUserId()
        val account = resolveActiveAccount(userId)
        val missingPermissions =
            permissions.filterNot { permission ->
                client
                    .checkPermission(
                        accountId = account.id.id,
                        request =
                            CheckAccountPermissionRequest(
                                userId = userId,
                                permission = permission,
                            ),
                    ).granted
            }

        if (missingPermissions.isNotEmpty()) {
            throw AccountPermissionDeniedException(
                accountId = account.id,
                requiredPermissions = permissions.mapTo(mutableSetOf()) { it.name },
            )
        }

        return account
    }

    override fun findByNickname(nickname: String): AccountSnapshot? =
        client
            .resolve(cristalixNickname = nickname)
            ?.let(::toSnapshot)

    private fun resolveActiveAccount(userId: UUID): AccountSnapshot =
        client
            .resolve(userId = userId)
            ?.let(::toSnapshot)
            ?: throw IllegalStateException("Active account is required")

    private fun toSnapshot(response: InternalAccountResponse): AccountSnapshot =
        AccountSnapshot(
            id = AccountId(response.accountId),
            nickname = response.cristalixNickname,
        )
}
