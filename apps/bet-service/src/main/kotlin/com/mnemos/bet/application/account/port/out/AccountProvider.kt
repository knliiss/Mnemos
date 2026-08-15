package com.mnemos.bet.application.account.port.out

import com.mnemos.bet.application.account.model.AccountSnapshot
import com.mnemos.common.core.model.permission.RolePermission

interface AccountProvider {
    fun requireActiveAccount(): AccountSnapshot

    fun requireActiveAccount(permission: RolePermission): AccountSnapshot = requireActiveAccount()

    fun findByNickname(nickname: String): AccountSnapshot?
}
