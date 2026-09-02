package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.account.ActiveAccountContext
import com.mnemos.account.application.context.account.InternalTelegramAccountContextProvider
import com.mnemos.account.application.query.account.member.AccountMemberQueryRepository
import com.mnemos.account.application.service.member.KickAccountMemberService
import com.mnemos.account.application.service.role.AssignRoleService
import com.mnemos.account.application.service.role.CreateRoleService
import com.mnemos.account.application.service.role.DeleteAccountRoleService
import com.mnemos.account.application.service.role.UpdateRoleHierarchyService
import com.mnemos.account.application.service.role.UpdateRoleService
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class InternalTelegramAccountManagementService(
    private val contextProvider: InternalTelegramAccountContextProvider,
    private val memberQueryRepository: AccountMemberQueryRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val roleRepository: RoleRepository,
    private val kickAccountMemberService: KickAccountMemberService,
    private val assignRoleService: AssignRoleService,
    private val createRoleService: CreateRoleService,
    private val updateRoleService: UpdateRoleService,
    private val deleteRoleService: DeleteAccountRoleService,
    private val updateRoleHierarchyService: UpdateRoleHierarchyService,
) {
    @Transactional(readOnly = true)
    fun getManagement(telegramId: TelegramId): InternalTelegramAccountManagement? {
        val context = contextProvider.requireActiveContext(telegramId) ?: return null

        return snapshot(context)
    }

    @Transactional
    fun kickMember(
        telegramId: TelegramId,
        memberId: AccountMemberId,
    ): InternalTelegramAccountManagement? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = RolePermission.MANAGE_MEMBERS,
            ) ?: return null
        val memberExists =
            memberQueryRepository
                .findAllByAccountId(context.account.id)
                .any { member -> member.id == memberId }

        if (memberExists) {
            kickAccountMemberService.kickMember(
                context = context,
                targetMemberId = memberId,
            )
        }

        return snapshot(context)
    }

    @Transactional
    fun setMemberRole(
        telegramId: TelegramId,
        memberId: AccountMemberId,
        roleId: RoleId,
        assigned: Boolean,
    ): InternalTelegramAccountManagement? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = RolePermission.ASSIGN_MEMBER_ROLES,
            ) ?: return null
        val currentlyAssigned =
            roleRepository
                .findAllByMemberId(
                    accountId = context.account.id,
                    memberId = memberId,
                ).any { role -> role.id == roleId }

        if (currentlyAssigned != assigned) {
            if (assigned) {
                assignRoleService.assignRole(
                    context = context,
                    memberId = memberId,
                    roleId = roleId,
                )
            } else {
                assignRoleService.revokeRole(
                    context = context,
                    memberId = memberId,
                    roleId = roleId,
                )
            }
        }

        return snapshot(context)
    }

    @Transactional
    fun createRole(
        telegramId: TelegramId,
        name: String,
    ): InternalTelegramAccountManagement? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = RolePermission.MANAGE_ROLES,
            ) ?: return null

        createRoleService.createRole(
            context = context,
            name = name,
            color = DEFAULT_ROLE_COLOR,
            permissions = emptySet(),
        )

        return snapshot(context)
    }

    @Transactional
    fun updateRoleDetails(
        telegramId: TelegramId,
        roleId: RoleId,
        name: String?,
        color: String?,
    ): InternalTelegramAccountManagement? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = RolePermission.MANAGE_ROLES,
            ) ?: return null

        updateRoleService.updateDetails(
            context = context,
            roleId = roleId,
            name = name,
            color = color,
        )

        return snapshot(context)
    }

    @Transactional
    fun setRolePermission(
        telegramId: TelegramId,
        roleId: RoleId,
        permission: RolePermission,
        enabled: Boolean,
    ): InternalTelegramAccountManagement? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = RolePermission.MANAGE_ROLES,
            ) ?: return null

        updateRoleService.updatePermissions(
            context = context,
            roleId = roleId,
            permissions = mapOf(permission to enabled),
        )

        return snapshot(context)
    }

    @Transactional
    fun deleteRole(
        telegramId: TelegramId,
        roleId: RoleId,
    ): InternalTelegramAccountManagement? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = RolePermission.MANAGE_ROLES,
            ) ?: return null
        val roleExists =
            roleRepository.findByIdAndAccountId(
                roleId = roleId,
                accountId = context.account.id,
            ) != null

        if (roleExists) {
            deleteRoleService.deleteRole(
                context = context,
                roleId = roleId,
            )
        }

        return snapshot(context)
    }

    @Transactional
    fun moveRole(
        telegramId: TelegramId,
        roleId: RoleId,
        targetIndex: Int,
    ): InternalTelegramAccountManagement? {
        val context =
            contextProvider.requirePermissionForUpdate(
                telegramId = telegramId,
                permission = RolePermission.MANAGE_ROLE_HIERARCHY,
            ) ?: return null
        val roles = roleRepository.findAllByAccountIdOrderByRank(context.account.id)
        val currentIndex = roles.indexOfFirst { role -> role.id == roleId }

        require(currentIndex >= 0) { "Role does not belong to the active account" }
        require(targetIndex in roles.indices) { "Role target index is outside the hierarchy" }

        if (currentIndex != targetIndex) {
            val reorderedIds = roles.map { role -> role.id }.toMutableList()
            val movedRoleId = reorderedIds.removeAt(currentIndex)

            reorderedIds.add(targetIndex, movedRoleId)

            updateRoleHierarchyService.updateHierarchy(
                context = context,
                orderedRoleIds = reorderedIds,
            )
        }

        return snapshot(context)
    }

    private fun snapshot(context: ActiveAccountContext): InternalTelegramAccountManagement {
        val accountId = context.account.id
        val roles = roleRepository.findAllByAccountIdOrderByRank(accountId)
        val roleSnapshots =
            roles.map { role ->
                InternalTelegramAccountRole(
                    id = role.id,
                    name = role.roleName.value,
                    color = role.roleColor.value,
                    hierarchyRank = role.hierarchyRank,
                    permissions = role.permissions.toSet(),
                )
            }
        val members =
            memberQueryRepository
                .findAllByAccountId(accountId)
                .map { member ->
                    val memberRoleIds =
                        roleRepository
                            .findAllByMemberId(
                                accountId = accountId,
                                memberId = member.id,
                            ).mapTo(linkedSetOf()) { role -> role.id }

                    InternalTelegramAccountMember(
                        id = member.id,
                        name = member.name,
                        owner = member.owner,
                        highestRoleName = member.highestRole?.name?.value,
                        highestRoleColor = member.highestRole?.color?.value,
                        roleIds = memberRoleIds,
                        joinedAt = member.joinedAt,
                    )
                }
        val actorPermissions =
            RolePermission.entries
                .filterTo(linkedSetOf()) { permission ->
                    accountMemberRepository.hasEffectivePermission(
                        memberId = context.member.id,
                        permission = permission,
                    )
                }

        return InternalTelegramAccountManagement(
            accountId = accountId,
            cristalixNickname = context.account.cristalixNickname.value,
            actorMemberId = context.member.id,
            actorPermissions = actorPermissions,
            members = members,
            roles = roleSnapshots,
        )
    }

    private companion object {
        const val DEFAULT_ROLE_COLOR = "#A9B1A4"
    }
}

data class InternalTelegramAccountManagement(
    val accountId: AccountId,
    val cristalixNickname: String,
    val actorMemberId: AccountMemberId,
    val actorPermissions: Set<RolePermission>,
    val members: List<InternalTelegramAccountMember>,
    val roles: List<InternalTelegramAccountRole>,
)

data class InternalTelegramAccountMember(
    val id: AccountMemberId,
    val name: String,
    val owner: Boolean,
    val highestRoleName: String?,
    val highestRoleColor: String?,
    val roleIds: Set<RoleId>,
    val joinedAt: Instant,
)

data class InternalTelegramAccountRole(
    val id: RoleId,
    val name: String,
    val color: String,
    val hierarchyRank: Long,
    val permissions: Set<RolePermission>,
)
