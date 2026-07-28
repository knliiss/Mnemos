package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.application.policy.role.RoleAccessGuard
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_ROLES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UpdateAccountBaseRoleService(
    private val contextProvider: ActiveAccountContextProvider,
    private val accountRepository: AccountRepository,
    private val roleAccessGuard: RoleAccessGuard,
) {
    @Transactional
    fun updateBaseRole(
        accountId: AccountId,
        roleId: RoleId?,
    ) {
        val context =
            contextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = MANAGE_ROLES,
            )

        val validatedRoleId =
            roleId?.let {
                roleAccessGuard
                    .requireCanUseAsBaseRole(
                        context = context,
                        roleId = it,
                    ).id
            }

        if (
            context.account.changeBaseRole(
                roleId = validatedRoleId,
                now = Instant.now(),
            )
        ) {
            accountRepository.update(context.account)
        }
    }
}
