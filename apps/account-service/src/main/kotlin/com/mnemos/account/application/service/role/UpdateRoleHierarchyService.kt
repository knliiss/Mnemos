package com.mnemos.account.application.service.role

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.policy.role.RoleAccessGuard
import com.mnemos.account.application.query.account.role.RoleView
import com.mnemos.account.application.query.account.role.RoleViewMapper.toView
import com.mnemos.account.domain.exception.role.InvalidRoleHierarchyException
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.MANAGE_ROLE_HIERARCHY
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateRoleHierarchyService(
    private val contextProvider: ActiveAccountContextProvider,
    private val roleRepository: RoleRepository,
    private val roleAccessGuard: RoleAccessGuard,
) {
    @Transactional
    fun updateHierarchy(
        accountId: AccountId,
        orderedRoleIds: List<RoleId>,
    ): List<RoleView> {
        val context =
            contextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = MANAGE_ROLE_HIERARCHY,
            )

        val currentRoles = roleRepository.findAllByAccountIdOrderByRank(accountId)

        validateRequestedOrder(
            accountId = accountId,
            roles = currentRoles,
            orderedRoleIds = orderedRoleIds,
        )

        val rolesById = currentRoles.associateBy { it.id }

        val reorderedRoles =
            orderedRoleIds.map { roleId ->
                rolesById.getValue(roleId)
            }

        roleAccessGuard.requireCanUpdateHierarchy(
            context = context,
            roles = currentRoles,
            orderedRoleIds = reorderedRoles.map { it.id },
        )

        val updatedRoles =
            reorderedRoles.mapIndexedNotNull { index, role ->
                val newRank = (index + 1L) * Role.RANK_STEP

                if (role.updateHierarchyRank(newRank)) role else null
            }

        if (updatedRoles.isNotEmpty()) {
            roleRepository.updateAll(updatedRoles)
        }

        return reorderedRoles.map { it.toView() }
    }

    private fun validateRequestedOrder(
        accountId: AccountId,
        roles: List<Role>,
        orderedRoleIds: List<RoleId>,
    ) {
        if (orderedRoleIds.size != orderedRoleIds.toSet().size) {
            throw InvalidRoleHierarchyException(
                accountId = accountId,
                reason = "Role order contains duplicate role IDs",
            )
        }

        val existingRoleIds = roles.mapTo(mutableSetOf()) { it.id }
        val requestedRoleIds = orderedRoleIds.toSet()

        if (requestedRoleIds != existingRoleIds) {
            throw InvalidRoleHierarchyException(
                accountId = accountId,
                reason = "Role order must contain every account role exactly once",
            )
        }
    }
}
