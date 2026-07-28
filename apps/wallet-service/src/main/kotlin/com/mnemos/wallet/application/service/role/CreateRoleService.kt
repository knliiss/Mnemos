package com.mnemos.accountservice.application.service.role

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.application.query.account.role.RoleView
import com.mnemos.accountservice.application.query.account.role.toView
import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.permission.RolePermission.CREATE_ROLES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateRoleService(
    private val roleRepository: RoleRepository,
    private val accountRepository: AccountRepository,
    private val accountContextProvider: ActiveAccountContextProvider,
) {
    private val basePermissions =
        setOf(
            RolePermission.CLAIM_DAILY_REWARD,
            RolePermission.VIEW_BETS,
            RolePermission.VIEW_BET_HISTORY,
            RolePermission.VIEW_PERSONALIZATIONS,
            RolePermission.APPLY_PERSONALIZATIONS,
        )

    @Transactional
    fun createRole(
        accountId: AccountId,
        name: String,
        color: String,
        permissions: Set<RolePermission>
    ): RoleView {
        val context = accountContextProvider.requirePermission(accountId, CREATE_ROLES)

        if (!accountRepository.lockById(accountId)) {
            throw AccountNotFoundException(accountId)
        }

        val maxRank = roleRepository.findMaxRank(accountId)
        val finalPermissions = permissions
            .ifEmpty { basePermissions }
            .toMutableSet()

        val role = Role.create(
            accountId = context.account.id,
            hierarchyRank = maxRank + Role.RANK_STEP,
            roleColor = HexColor(color),
            roleName = AccountRoleName(name),
            permissions = finalPermissions
        )

        return roleRepository.create(role).toView()
    }
}
