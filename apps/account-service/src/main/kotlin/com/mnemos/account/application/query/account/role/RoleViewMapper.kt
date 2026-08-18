package com.mnemos.account.application.query.account.role

import com.mnemos.account.domain.model.account.role.Role

object RoleViewMapper {
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
}
