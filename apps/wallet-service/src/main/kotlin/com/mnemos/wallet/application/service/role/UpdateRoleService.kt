package com.mnemos.accountservice.application.service.role

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.application.policy.role.RoleAccessGuard
import com.mnemos.accountservice.application.query.account.role.RoleView
import com.mnemos.accountservice.application.query.account.role.toView
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.permission.RolePermission.EDIT_ROLE_DETAILS
import com.mnemos.common.core.model.permission.RolePermission.EDIT_ROLE_PERMISSIONS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateRoleService(
    private val accountContextProvider: ActiveAccountContextProvider,
    private val roleRepository: RoleRepository,
    private val roleAccessGuard: RoleAccessGuard,
) {
    @Transactional
    fun updateDetails(
        accountId: AccountId,
        roleId: RoleId,
        name: String?,
        color: String?,
    ): RoleView {
        val context = accountContextProvider.requirePermission(accountId, EDIT_ROLE_DETAILS)

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

        if (!updated) return role.toView()

        return roleRepository.update(role).toView()
    }

    @Transactional
    fun updatePermissions(
        accountId: AccountId,
        roleId: RoleId,
        permissions: Map<RolePermission, Boolean>,
    ): RoleView {
        val context = accountContextProvider.requirePermission(accountId, EDIT_ROLE_PERMISSIONS)

        val role =
            roleAccessGuard.requireCanManageRole(
                context = context,
                roleId = roleId,
            )

        if (!role.updatePermissions(permissions)) return role.toView()

        return roleRepository.update(role).toView()
    }
}
