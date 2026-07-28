package com.mnemos.account.domain.model.account.role

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import java.time.Instant

class Role private constructor(
    val id: RoleId,
    val accountId: AccountId,
    roleColor: HexColor,
    roleName: AccountRoleName,
    hierarchyRank: Long,
    private val rolePermissions: MutableSet<RolePermission>,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    val permissions: Set<RolePermission>
        get() = rolePermissions

    var updatedAt = updatedAt
        private set

    var roleColor = roleColor
        private set

    var roleName = roleName
        private set

    var hierarchyRank = hierarchyRank
        private set

    companion object {
        const val RANK_STEP: Long = 1_000_000L

        fun create(
            accountId: AccountId,
            roleColor: HexColor,
            roleName: AccountRoleName,
            hierarchyRank: Long,
            permissions: MutableSet<RolePermission>,
        ): Role {
            val now = Instant.now()

            return Role(
                id = RoleId.generate(),
                accountId = accountId,
                roleColor = roleColor,
                roleName = roleName,
                hierarchyRank = hierarchyRank,
                rolePermissions = permissions,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun restore(
            id: RoleId,
            accountId: AccountId,
            roleColor: HexColor,
            roleName: AccountRoleName,
            hierarchyRank: Long,
            permissions: Set<RolePermission>,
            createdAt: Instant,
            updatedAt: Instant,
        ): Role =
            Role(
                id = id,
                accountId = accountId,
                roleColor = roleColor,
                roleName = roleName,
                hierarchyRank = hierarchyRank,
                rolePermissions = permissions.toMutableSet(),
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
    }

    fun updatePermissions(permissions: Map<RolePermission, Boolean>): Boolean {
        var hasChanges = false

        permissions.forEach { (permission, enabled) ->
            if (enabled) {
                hasChanges = rolePermissions.add(permission) || hasChanges
            } else {
                hasChanges = rolePermissions.remove(permission) || hasChanges
            }
        }

        if (!hasChanges) return false

        updatedAt = Instant.now()
        return true
    }

    fun updateRoleName(roleName: AccountRoleName): Boolean {
        if (this.roleName == roleName) return false

        this.roleName = roleName
        updatedAt = Instant.now()
        return true
    }

    fun updateRoleColor(roleColor: HexColor): Boolean {
        if (this.roleColor == roleColor) return false

        this.roleColor = roleColor
        updatedAt = Instant.now()
        return true
    }

    fun updateHierarchyRank(newRank: Long): Boolean {
        if (hierarchyRank == newRank) return false

        hierarchyRank = newRank
        updatedAt = Instant.now()
        return true
    }
}
