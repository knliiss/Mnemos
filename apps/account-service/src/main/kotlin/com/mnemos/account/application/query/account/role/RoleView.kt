package com.mnemos.account.application.query.account.role

import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.permission.RolePermission
import java.time.Instant

data class RoleView(
    val id: RoleId,
    val name: String,
    val color: String,
    val hierarchyRank: Long,
    val permissions: Set<RolePermission>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun Role.toView(): RoleView =
    RoleView(
        id = id,
        name = roleName.value,
        color = roleColor.value,
        hierarchyRank = hierarchyRank,
        permissions = permissions,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
