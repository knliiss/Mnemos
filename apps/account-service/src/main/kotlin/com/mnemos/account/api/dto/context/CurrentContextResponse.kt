package com.mnemos.account.api.dto.context

import com.mnemos.account.api.dto.account.AccountDetailsResponse
import com.mnemos.account.api.dto.account.AccountSummaryResponse
import com.mnemos.account.api.dto.user.UserResponse
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.UserRole

data class CurrentContextResponse(
    val user: UserResponse,
    val systemRoles: Set<UserRole>,
    val availableAccounts: List<AccountSummaryResponse>,
    val activeAccount: AccountDetailsResponse?,
    val effectivePermissions: Set<RolePermission>,
)
