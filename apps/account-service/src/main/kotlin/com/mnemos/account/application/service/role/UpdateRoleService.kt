package com.mnemos.account.application.service.role

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.policy.role.RoleAccessGuard
import com.mnemos.account.application.policy.role.RolePermissionMutationGuard
import com.mnemos.account.application.query.account.role.RoleView
import com.mnemos.account.application.query.account.role.RoleViewMapper.toView
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.HexColor
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_ROLES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateRoleService(
    private val accountContextProvider: ActiveAccountContextProvider,
    private val roleRepository: RoleRepository,
    private val roleAccessGuard: RoleAccessGuard,
    private val rolePermissionMutationGuard: RolePermissionMutationGuard,
) {
    @Transactional
    fun updateDetails(
        accountId: AccountId,
        roleId: RoleId,
        name: String?,
        color: String?,
    ): RoleView {
        val context =
            accountContextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = MANAGE_ROLES,
            )
        val role =
            roleAccessGuard.requireCanManageRole(
                context = context,
                roleId = roleId,
            )

        var updated = false

        if (!color.isNullOrBlank()) {
            updated = role.updateRoleColor(HexColor(color))
        }

        if (!name.isNullOrBlank()) {
            updated = role.updateRoleName(AccountRoleName(name)) || updated
        }

        if (!updated) {
            return role.toView()
        }

        return roleRepository.update(role).toView()
    }

    @Transactional
    fun updatePermissions(
        accountId: AccountId,
        roleId: RoleId,
        permissions: Map<RolePermission, Boolean>,
    ): RoleView {
        val context =
            accountContextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = MANAGE_ROLES,
            )
        val role =
            roleAccessGuard.requireCanManageRole(
                context = context,
                roleId = roleId,
            )

        rolePermissionMutationGuard.requireRoleNotAssignedToActor(
            context = context,
            role = role,
        )
        roleAccessGuard.requireCanGrantPermissions(
            context = context,
            permissions = permissions.filterValues { enabled -> enabled }.keys,
        )

        if (!role.updatePermissions(permissions)) {
            return role.toView()
        }

        return roleRepository.update(role).toView()
    }
}
