package com.mnemos.accountservice.api.dto.account.role

import com.mnemos.accountservice.application.query.account.role.RoleView
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

fun RoleView.toResponse(): RoleResponse =
    RoleResponse(
        id = id.value,
        name = name,
        color = color,
        hierarchyRank = hierarchyRank,
        permissions =
            RolePermission.entries.associateWith {
                it in permissions
            },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
