package com.mnemos.account.api.dto.account.role

import com.mnemos.common.core.model.permission.RolePermission
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateRoleRequest(
    @field:NotBlank
    @field:Size(max = 32)
    val name: String,
    @field:Pattern(
        regexp = "^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$",
        message = "Role color must use #RRGGBB or #RRGGBBAA format",
    )
    val color: String,
    val permissions: Set<RolePermission> = emptySet(),
)

data class UpdateRoleDetailsRequest(
    @field:Size(min = 1, max = 32)
    val name: String? = null,
    @field:Pattern(
        regexp = "^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$",
        message = "Role color must use #RRGGBB or #RRGGBBAA format",
    )
    val color: String? = null,
)

data class UpdateRolePermissionsRequest(
    @field:NotEmpty
    val permissions: Map<RolePermission, Boolean>,
)

data class UpdateRoleHierarchyRequest(
    @field:NotEmpty
    val orderedRoleIds: List<UUID>,
)
