package com.mnemos.reward.adapter.out.account

import com.mnemos.common.core.model.permission.RolePermission
import java.util.UUID

data class CheckAccountPermissionRequest(
    val userId: UUID,
    val permission: RolePermission,
)
