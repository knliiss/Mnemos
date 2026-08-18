package com.mnemos.account.api.dto.account.role

import com.mnemos.account.application.query.account.role.RoleView
import com.mnemos.common.core.model.permission.RolePermission

object RoleApiMapper {
    fun RoleView.toResponse(): RoleResponse =
        RoleResponse(
            id = id.id,
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
}
