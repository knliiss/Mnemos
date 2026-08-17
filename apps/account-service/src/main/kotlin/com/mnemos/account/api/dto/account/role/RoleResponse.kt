package com.mnemos.account.api.dto.account.role

import com.mnemos.common.core.model.permission.RolePermission
import java.time.Instant
import java.util.UUID

data class RoleResponse(
    val id: UUID,
    val name: String,
    val color: String,
    val hierarchyRank: Long,
    val permissions: Map<RolePermission, Boolean>,
    val createdAt: Instant,
    val updatedAt: Instant,
)
