package com.mnemos.wallet.adapter.out.account.dto

import com.mnemos.common.core.model.permission.RolePermission
import java.util.UUID

data class CheckAccountPermissionRequest(
    val userId: UUID,
    val permission: RolePermission,
)

data class CheckAccountPermissionResponse(
    val granted: Boolean,
)
