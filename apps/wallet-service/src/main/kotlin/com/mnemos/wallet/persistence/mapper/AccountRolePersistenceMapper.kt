package com.mnemos.accountservice.persistence.mapper

import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.persistence.entity.role.RoleEntity
import com.mnemos.common.core.model.id.AccountId

object AccountRolePersistenceMapper {
    fun Role.toEntity(): RoleEntity =
        RoleEntity(
            id = id.value,
            accountId = accountId.value,
            roleName = roleName.value,
            roleColor = roleColor.value,
            hierarchyRank = hierarchyRank,
            permissions = permissions.toMutableSet(),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun RoleEntity.toDomain(): Role =
        Role.restore(
            id = RoleId(id),
            accountId = AccountId(accountId),
            roleColor = HexColor(roleColor),
            roleName = AccountRoleName(roleName),
            hierarchyRank = hierarchyRank,
            permissions = permissions,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun RoleEntity.updateFrom(role: Role) {
        permissions.clear()
        permissions.addAll(role.permissions)

        roleName = role.roleName.value
        roleColor = role.roleColor.value
        updatedAt = role.updatedAt
        hierarchyRank = role.hierarchyRank
    }
}
