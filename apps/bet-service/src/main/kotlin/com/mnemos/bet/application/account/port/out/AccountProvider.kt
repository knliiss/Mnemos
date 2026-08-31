package com.mnemos.bet.application.account.port.out

import com.mnemos.bet.application.account.model.AccountSnapshot
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission

interface AccountProvider {
    fun requireActiveAccount(): AccountSnapshot

    fun requireActiveAccount(permission: RolePermission): AccountSnapshot

    fun requireActiveAccount(userId: UserId): AccountSnapshot =
        throw UnsupportedOperationException(
            "Explicit user account resolution is not supported",
        )

    fun requireActiveAccount(
        userId: UserId,
        permission: RolePermission,
    ): AccountSnapshot =
        throw UnsupportedOperationException(
            "Explicit user permission resolution is not supported",
        )

    fun findByNickname(nickname: String): AccountSnapshot?
}
