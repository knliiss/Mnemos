package com.mnemos.wallet.application.account.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission

fun interface AccountPermissionProvider {
    fun hasPermission(
        accountId: AccountId,
        userId: UserId,
        permission: RolePermission,
    ): Boolean
}
