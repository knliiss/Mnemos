package com.mnemos.personalization.application.account.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission

interface PersonalizationAccountAccess {
    fun requireActiveAccount(userId: UserId): AccountId

    fun hasPermission(
        accountId: AccountId,
        userId: UserId,
        permission: RolePermission,
    ): Boolean
}
