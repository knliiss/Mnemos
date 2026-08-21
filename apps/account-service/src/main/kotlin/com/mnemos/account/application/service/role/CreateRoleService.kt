package com.mnemos.account.application.service.role

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.policy.role.RoleAccessGuard
import com.mnemos.account.application.query.account.role.RoleView
import com.mnemos.account.application.query.account.role.RoleViewMapper.toView
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.HexColor
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.permission.RolePermission.CREATE_ROLES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateRoleService(
    private val roleRepository: RoleRepository,
    private val accountContextProvider: ActiveAccountContextProvider,
    private val roleAccessGuard: RoleAccessGuard,
) {
    @Transactional
    fun createRole(
        accountId: AccountId,
        name: String,
        color: String,
        permissions: Set<RolePermission>,
    ): RoleView {
        val context =
            accountContextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = CREATE_ROLES,
            )

        val hierarchyRank = prepareHierarchyRank(context.account.id)
        val finalPermissions = permissions.toMutableSet()

        roleAccessGuard.requireCanGrantPermissions(
            context = context,
            permissions = finalPermissions,
        )

        val role =
            Role.create(
                accountId = context.account.id,
                hierarchyRank = hierarchyRank,
                roleColor = HexColor(color),
                roleName = AccountRoleName(name),
                permissions = finalPermissions,
            )

        return roleRepository.create(role).toView()
    }

    private fun prepareHierarchyRank(accountId: AccountId): Long {
        val lowestRole = roleRepository.findAllByAccountIdOrderByRank(accountId).lastOrNull()
            ?: return Role.RANK_STEP

        val hierarchyRank = lowestRole.hierarchyRank
        val nextRank = Math.addExact(hierarchyRank, Role.RANK_STEP)

        lowestRole.updateHierarchyRank(nextRank)
        roleRepository.update(lowestRole)

        return hierarchyRank
    }
}
