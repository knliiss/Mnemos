package com.mnemos.reward.application.daily.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission

data class ActiveAccountContext(
    val accountId: AccountId,
    val userId: UserId,
)

fun interface ActiveAccountProvider {
    fun getActiveAccountContext(): ActiveAccountContext

    fun getActiveAccountContext(permission: RolePermission): ActiveAccountContext = getActiveAccountContext()
}
