package com.mnemos.account.api.dto.internal.permission

import com.mnemos.common.core.model.permission.RolePermission
import java.util.UUID

data class CheckAccountPermissionRequest(
    val userId: UUID,
    val permission: RolePermission,
)

data class CheckAccountPermissionResponse(
    val granted: Boolean,
)

data class EffectivePermissionsResponse(
    val permissions: Set<RolePermission>,
)
