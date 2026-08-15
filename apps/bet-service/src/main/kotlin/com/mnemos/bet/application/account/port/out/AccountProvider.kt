package com.mnemos.bet.application.account.port.out

import com.mnemos.bet.application.account.model.AccountSnapshot
import com.mnemos.common.core.model.permission.RolePermission

interface AccountProvider {
    fun requireActiveAccount(): AccountSnapshot

    fun requireActiveAccount(permission: RolePermission): AccountSnapshot = requireActiveAccount()

    fun requireActiveAccount(permissions: Set<RolePermission>): AccountSnapshot {
        require(permissions.isNotEmpty()) { "At least one account permission is required" }

        var account: AccountSnapshot? = null

        permissions.forEach { permission ->
            account = requireActiveAccount(permission)
        }

        return requireNotNull(account)
    }

    fun findByNickname(nickname: String): AccountSnapshot?
}
