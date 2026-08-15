package com.mnemos.account.application.service.role

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.policy.role.RoleAccessGuard
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.DELETE_ROLES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteAccountRoleService(
    private val contextProvider: ActiveAccountContextProvider,
    private val roleAccessGuard: RoleAccessGuard,
    private val roleRepository: RoleRepository,
) {
    @Transactional
    fun deleteRole(
        accountId: AccountId,
        roleId: RoleId,
    ) {
        val context =
            contextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = DELETE_ROLES,
            )

        val role =
            roleAccessGuard.requireCanManageRole(
                context = context,
                roleId = roleId,
            )

        roleRepository.deleteById(role.id)
    }
}
