package com.mnemos.accountservice.application.service.role

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.application.policy.role.RoleAccessGuard
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.DELETE_ROLES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteAccountRoleService(
    private val contextProvider: ActiveAccountContextProvider,
    private val roleAccessGuard: RoleAccessGuard,
    private val accountRepository: AccountRepository,
    private val roleRepository: RoleRepository,
) {
    @Transactional
    fun deleteRole(
        accountId: AccountId,
        roleId: RoleId,
    ) {
        val context =
            contextProvider.requirePermission(
                accountId = accountId,
                permission = DELETE_ROLES,
            )

        accountRepository.lockById(accountId)

        val role =
            roleAccessGuard.requireCanManageRole(
                context = context,
                roleId = roleId,
            )

        roleRepository.deleteById(role.id)
    }
}
