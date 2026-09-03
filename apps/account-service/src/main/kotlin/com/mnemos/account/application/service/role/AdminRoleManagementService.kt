package com.mnemos.account.application.service.role

import com.mnemos.account.application.query.account.role.RoleView
import com.mnemos.account.application.query.account.role.RoleViewMapper.toView
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.account.domain.exception.role.InvalidRoleHierarchyException
import com.mnemos.account.domain.exception.role.RoleNotFoundException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.HexColor
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.role.RoleAssignmentRepository
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AdminRoleManagementService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val roleRepository: RoleRepository,
    private val roleAssignmentRepository: RoleAssignmentRepository,
) {
    @Transactional
    fun createRole(
        accountId: AccountId,
        name: String,
        color: String,
        permissions: Set<RolePermission>,
    ): RoleView {
        requireAccount(accountId)

        val roles = roleRepository.findAllByAccountIdOrderByRank(accountId)
        val hierarchyRank =
            if (roles.isEmpty()) {
                Role.RANK_STEP
            } else {
                Math.addExact(roles.last().hierarchyRank, Role.RANK_STEP)
            }

        val role =
            Role.create(
                accountId = accountId,
                hierarchyRank = hierarchyRank,
                roleColor = HexColor(color),
                roleName = AccountRoleName(name),
                permissions = permissions.toMutableSet(),
            )

        return roleRepository.create(role).toView()
    }

    @Transactional
    fun updateDetails(
        accountId: AccountId,
        roleId: RoleId,
        name: String?,
        color: String?,
    ): RoleView {
        val role = requireRole(accountId, roleId)
        var updated = false

        if (!color.isNullOrBlank()) {
            updated = role.updateRoleColor(HexColor(color))
        }

        if (!name.isNullOrBlank()) {
            updated = role.updateRoleName(AccountRoleName(name)) || updated
        }

        return if (updated) {
            roleRepository.update(role).toView()
        } else {
            role.toView()
        }
    }

    @Transactional
    fun updatePermissions(
        accountId: AccountId,
        roleId: RoleId,
        permissions: Map<RolePermission, Boolean>,
    ): RoleView {
        val role = requireRole(accountId, roleId)

        if (!role.updatePermissions(permissions)) {
            return role.toView()
        }

        return roleRepository.update(role).toView()
    }

    @Transactional
    fun updateHierarchy(
        accountId: AccountId,
        orderedRoleIds: List<RoleId>,
    ): List<RoleView> {
        requireAccount(accountId)

        val currentRoles = roleRepository.findAllByAccountIdOrderByRank(accountId)
        validateRequestedOrder(
            accountId = accountId,
            roles = currentRoles,
            orderedRoleIds = orderedRoleIds,
        )

        val rolesById = currentRoles.associateBy { role -> role.id }
        val reorderedRoles = orderedRoleIds.map(rolesById::getValue)
        val updatedRoles =
            reorderedRoles.mapIndexedNotNull { index, role ->
                val hierarchyRank = (index + 1L) * Role.RANK_STEP

                if (role.updateHierarchyRank(hierarchyRank)) role else null
            }

        if (updatedRoles.isNotEmpty()) {
            roleRepository.updateAll(updatedRoles)
        }

        return reorderedRoles.map { role -> role.toView() }
    }

    @Transactional
    fun updateBaseRole(
        accountId: AccountId,
        roleId: RoleId?,
    ) {
        val account = requireAccount(accountId)
        val validatedRoleId = roleId?.let { requireRole(accountId, it).id }

        if (account.changeBaseRole(validatedRoleId, Instant.now())) {
            accountRepository.update(account)
        }
    }

    @Transactional
    fun deleteRole(
        accountId: AccountId,
        roleId: RoleId,
    ) {
        val account = requireAccount(accountId)
        val role = requireRole(accountId, roleId)

        if (account.baseRoleId == role.id) {
            account.changeBaseRole(null, Instant.now())
            accountRepository.update(account)
        }

        roleRepository.deleteById(role.id)
    }

    @Transactional
    fun assignRole(
        accountId: AccountId,
        memberId: AccountMemberId,
        roleId: RoleId,
    ) {
        requireMember(accountId, memberId)
        requireRole(accountId, roleId)

        if (roleAssignmentRepository.exists(memberId, roleId)) {
            return
        }

        roleAssignmentRepository.create(
            RoleAssignment.createAdministrative(
                accountId = accountId,
                accountMemberId = memberId,
                roleId = roleId,
                now = Instant.now(),
            ),
        )
    }

    @Transactional
    fun revokeRole(
        accountId: AccountId,
        memberId: AccountMemberId,
        roleId: RoleId,
    ) {
        requireMember(accountId, memberId)
        requireRole(accountId, roleId)

        roleAssignmentRepository.remove(
            roleId = roleId,
            accountMemberId = memberId,
        )
    }

    private fun requireAccount(accountId: AccountId) =
        accountRepository.findByIdForUpdate(accountId)
            ?: throw AccountNotFoundException(accountId)

    private fun requireRole(
        accountId: AccountId,
        roleId: RoleId,
    ): Role =
        roleRepository.findByIdAndAccountId(
            roleId = roleId,
            accountId = accountId,
        ) ?: throw RoleNotFoundException(
            roleId = roleId,
            accountId = accountId,
        )

    private fun requireMember(
        accountId: AccountId,
        memberId: AccountMemberId,
    ) =
        accountMemberRepository.findByIdAndAccountId(
            memberId = memberId,
            accountId = accountId,
        ) ?: throw AccountMemberNotFoundException(
            accountId = accountId,
            memberId = memberId,
        )

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

        val existingRoleIds = roles.mapTo(mutableSetOf()) { role -> role.id }

        if (orderedRoleIds.toSet() != existingRoleIds) {
            throw InvalidRoleHierarchyException(
                accountId = accountId,
                reason = "Role order must contain every account role exactly once",
            )
        }
    }
}
