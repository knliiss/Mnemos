package com.mnemos.account.application.service

import com.mnemos.account.application.service.role.AssignRoleService
import com.mnemos.account.application.service.role.CreateRoleService
import com.mnemos.account.application.service.role.DeleteAccountRoleService
import com.mnemos.account.application.service.role.RoleQueryService
import com.mnemos.account.application.service.role.UpdateRoleHierarchyService
import com.mnemos.account.application.service.role.UpdateRoleService
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.role.InvalidRoleHierarchyException
import com.mnemos.account.domain.exception.role.RoleNotFoundException
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignmentActor
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignmentReason
import com.mnemos.account.support.InMemoryAccountMemberRepository
import com.mnemos.account.support.InMemoryAccountRepository
import com.mnemos.account.support.InMemoryRoleAssignmentRepository
import com.mnemos.account.support.InMemoryRoleRepository
import com.mnemos.account.support.InMemoryUserRepository
import com.mnemos.account.support.SecurityContextTest
import com.mnemos.account.support.account
import com.mnemos.account.support.activeAccountContextProvider
import com.mnemos.account.support.authenticate
import com.mnemos.account.support.member
import com.mnemos.account.support.role
import com.mnemos.account.support.roleAccessGuard
import com.mnemos.account.support.user
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class RoleServicesTest : SecurityContextTest() {
    @Test
    fun `create first role at first rank with requested permissions`() {
        val context = context(RolePermission.CREATE_ROLES, owner = true)
        val roles = InMemoryRoleRepository()
        val requested = setOf(RolePermission.MANAGE_BETS, RolePermission.PLACE_BETS)

        val result =
            CreateRoleService(roles, context.accounts, context.provider)
                .createRole(context.accountId, "Bettor", "#AABBCC", requested)

        assertEquals(Role.RANK_STEP, result.hierarchyRank)
        assertEquals(requested, result.permissions)
        assertEquals(1, roles.createCount)
        assertEquals(listOf(context.accountId), context.accounts.lockCalls)
    }

    @Test
    fun `create role uses base permissions only when request is empty`() {
        val context = context(RolePermission.CREATE_ROLES, owner = true)
        val roles = InMemoryRoleRepository()

        val result =
            CreateRoleService(roles, context.accounts, context.provider)
                .createRole(context.accountId, "Default", "#AABBCC", emptySet())

        assertEquals(
            setOf(
                RolePermission.CLAIM_DAILY_REWARD,
                RolePermission.VIEW_BETS,
                RolePermission.VIEW_BET_HISTORY,
                RolePermission.VIEW_PERSONALIZATIONS,
                RolePermission.APPLY_PERSONALIZATIONS,
            ),
            result.permissions,
        )
    }

    @Test
    fun `create role appends after current maximum rank`() {
        val context = context(RolePermission.CREATE_ROLES, owner = true)
        val existing = role(accountId = context.accountId, rank = 4 * Role.RANK_STEP)
        val roles = InMemoryRoleRepository(listOf(existing))

        val result =
            CreateRoleService(roles, context.accounts, context.provider)
                .createRole(context.accountId, "New", "#AABBCC", emptySet())

        assertEquals(5 * Role.RANK_STEP, result.hierarchyRank)
    }

    @Test
    fun `update details skips repository when no effective change`() {
        val context = context(RolePermission.EDIT_ROLE_DETAILS, owner = true)
        val role = role(accountId = context.accountId, name = "Role", color = "#AABBCC")
        val roles = InMemoryRoleRepository(listOf(role))
        val service = UpdateRoleService(context.provider, roles, roleAccessGuard(roles, context.members))

        val result = service.updateDetails(context.accountId, role.id, "Role", "#AABBCC")

        assertEquals("Role", result.name)
        assertEquals(0, roles.updateCount)
    }

    @Test
    fun `update details applies partial and combined changes`() {
        val context = context(RolePermission.EDIT_ROLE_DETAILS, owner = true)
        val role = role(accountId = context.accountId, name = "Old", color = "#AABBCC")
        val roles = InMemoryRoleRepository(listOf(role))
        val service = UpdateRoleService(context.provider, roles, roleAccessGuard(roles, context.members))

        val result = service.updateDetails(context.accountId, role.id, "New", "#112233")

        assertEquals("New", result.name)
        assertEquals("#112233", result.color)
        assertEquals(1, roles.updateCount)
    }

    @Test
    fun `blank detail fields are treated as absent`() {
        val context = context(RolePermission.EDIT_ROLE_DETAILS, owner = true)
        val role = role(accountId = context.accountId, name = "Old", color = "#AABBCC")
        val roles = InMemoryRoleRepository(listOf(role))

        val result =
            UpdateRoleService(context.provider, roles, roleAccessGuard(roles, context.members))
                .updateDetails(context.accountId, role.id, " ", "")

        assertEquals("Old", result.name)
        assertEquals(0, roles.updateCount)
    }

    @Test
    fun `update permissions persists only effective changes`() {
        val context = context(RolePermission.EDIT_ROLE_PERMISSIONS, owner = true)
        val role = role(accountId = context.accountId, permissions = setOf(RolePermission.VIEW_BETS))
        val roles = InMemoryRoleRepository(listOf(role))
        val service = UpdateRoleService(context.provider, roles, roleAccessGuard(roles, context.members))

        service.updatePermissions(context.accountId, role.id, mapOf(RolePermission.VIEW_BETS to true))
        val changed =
            service.updatePermissions(
                context.accountId,
                role.id,
                mapOf(RolePermission.VIEW_BETS to false, RolePermission.MANAGE_BETS to true),
            )

        assertEquals(setOf(RolePermission.MANAGE_BETS), changed.permissions)
        assertEquals(1, roles.updateCount)
    }

    @Test
    fun `assign role records manual actor and role`() {
        val context = context(RolePermission.ASSIGN_MEMBER_ROLES, owner = true)
        val target = member(accountId = context.accountId)
        context.members.members[target.id] = target
        val role = role(accountId = context.accountId)
        val roles = InMemoryRoleRepository(listOf(role))
        val assignments = InMemoryRoleAssignmentRepository()
        val service =
            AssignRoleService(
                context.provider,
                roleAccessGuard(roles, context.members),
                assignments,
            )

        service.assignRole(context.accountId, target.id, role.id)

        val assignment = assignments.find(role.id, target.id)!!
        assertEquals(RoleAssignmentActor.User(context.user.id), assignment.assignedBy)
        assertEquals(RoleAssignmentReason.MANUAL, assignment.reason)
        assertEquals(context.accountId, assignment.accountId)
    }

    @Test
    fun `revoke role validates same access policy and removes assignment`() {
        val context = context(RolePermission.ASSIGN_MEMBER_ROLES, owner = true)
        val target = member(accountId = context.accountId)
        context.members.members[target.id] = target
        val role = role(accountId = context.accountId)
        val roles = InMemoryRoleRepository(listOf(role))
        val assignments = InMemoryRoleAssignmentRepository()
        val service = AssignRoleService(context.provider, roleAccessGuard(roles, context.members), assignments)
        service.assignRole(context.accountId, target.id, role.id)

        service.revokeRole(context.accountId, target.id, role.id)

        assertFalse(assignments.exists(target.id, role.id))
        assertEquals(1, assignments.removeCount)
    }

    @Test
    fun `delete role validates role and deletes exact id`() {
        val context = context(RolePermission.DELETE_ROLES, owner = true)
        val role = role(accountId = context.accountId)
        val roles = InMemoryRoleRepository(listOf(role))

        DeleteAccountRoleService(
            context.provider,
            roleAccessGuard(roles, context.members),
            context.accounts,
            roles,
        ).deleteRole(context.accountId, role.id)

        assertEquals(listOf(role.id), roles.deletedIds)
        assertEquals(listOf(context.accountId), context.accounts.lockCalls)
    }

    @Test
    fun `role query scopes role lookup to active account`() {
        val context = context(RolePermission.VIEW_ROLES, owner = true)
        val first = role(accountId = context.accountId, rank = Role.RANK_STEP)
        val second = role(accountId = context.accountId, rank = 2 * Role.RANK_STEP)
        val roles = InMemoryRoleRepository(listOf(first, second))
        roles.memberRoles[context.member.id] = mutableSetOf(second.id)
        val service = RoleQueryService(context.provider, roles)

        assertEquals(listOf(first.id, second.id), service.getRoles(context.accountId).map { it.id })
        assertEquals(first.id, service.getRole(context.accountId, first.id).id)
        assertEquals(listOf(second.id), service.getMemberRoles(context.accountId, context.member.id).map { it.id })
        assertThrows<RoleNotFoundException> {
            service.getRole(
                context.accountId,
                com.mnemos.account.domain.model.account.role
                    .RoleId(UUID.randomUUID()),
            )
        }
    }

    @Test
    fun `hierarchy update rejects unavailable locked account`() {
        val context = context(RolePermission.MANAGE_ROLE_HIERARCHY, owner = true)
        context.accounts.lockResultOverride = false

        assertThrows<AccountNotFoundException> {
            UpdateRoleHierarchyService(
                context.provider,
                context.accounts,
                InMemoryRoleRepository(),
                roleAccessGuard(InMemoryRoleRepository(), context.members),
            ).updateHierarchy(context.accountId, emptyList())
        }
    }

    @Test
    fun `hierarchy update rejects duplicates and incomplete role sets`() {
        val context = context(RolePermission.MANAGE_ROLE_HIERARCHY, owner = true)
        val first = role(accountId = context.accountId, rank = Role.RANK_STEP)
        val second = role(accountId = context.accountId, rank = 2 * Role.RANK_STEP)
        val roles = InMemoryRoleRepository(listOf(first, second))
        val service =
            UpdateRoleHierarchyService(
                context.provider,
                context.accounts,
                roles,
                roleAccessGuard(roles, context.members),
            )

        assertThrows<InvalidRoleHierarchyException> {
            service.updateHierarchy(context.accountId, listOf(first.id, first.id))
        }
        assertThrows<InvalidRoleHierarchyException> {
            service.updateHierarchy(context.accountId, listOf(first.id))
        }
    }

    @Test
    fun `hierarchy update normalizes all ranks and persists only changed roles`() {
        val context = context(RolePermission.MANAGE_ROLE_HIERARCHY, owner = true)
        val first = role(accountId = context.accountId, rank = Role.RANK_STEP)
        val second = role(accountId = context.accountId, rank = 2 * Role.RANK_STEP)
        val third = role(accountId = context.accountId, rank = 3 * Role.RANK_STEP)
        val roles = InMemoryRoleRepository(listOf(first, second, third))
        val service =
            UpdateRoleHierarchyService(
                context.provider,
                context.accounts,
                roles,
                roleAccessGuard(roles, context.members),
            )

        val result = service.updateHierarchy(context.accountId, listOf(third.id, first.id, second.id))

        assertEquals(listOf(third.id, first.id, second.id), result.map { it.id })
        assertEquals(listOf(Role.RANK_STEP, 2 * Role.RANK_STEP, 3 * Role.RANK_STEP), result.map { it.hierarchyRank })
        assertEquals(1, roles.updateAllCount)
    }

    @Test
    fun `hierarchy update avoids save when order and normalized ranks are unchanged`() {
        val context = context(RolePermission.MANAGE_ROLE_HIERARCHY, owner = true)
        val first = role(accountId = context.accountId, rank = Role.RANK_STEP)
        val second = role(accountId = context.accountId, rank = 2 * Role.RANK_STEP)
        val roles = InMemoryRoleRepository(listOf(first, second))
        val service =
            UpdateRoleHierarchyService(
                context.provider,
                context.accounts,
                roles,
                roleAccessGuard(roles, context.members),
            )

        service.updateHierarchy(context.accountId, listOf(first.id, second.id))

        assertEquals(0, roles.updateAllCount)
    }

    private data class Context(
        val accountId: AccountId,
        val user: com.mnemos.account.domain.model.user.User,
        val member: com.mnemos.account.domain.model.account.AccountMember,
        val accounts: InMemoryAccountRepository,
        val members: InMemoryAccountMemberRepository,
        val provider: com.mnemos.account.application.context.account.ActiveAccountContextProvider,
    )

    private fun context(
        permission: RolePermission,
        owner: Boolean,
    ): Context {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val ownerMemberId =
            if (owner) {
                member.id
            } else {
                com.mnemos.account.domain.model.account
                    .AccountMemberId(UUID.randomUUID())
            }
        val account = account(id = accountId, ownerMemberId = ownerMemberId)
        val users = InMemoryUserRepository(listOf(user))
        val accounts = InMemoryAccountRepository(listOf(account))
        val members = InMemoryAccountMemberRepository(listOf(member))
        members.permissions[member.id] = mutableSetOf(permission)
        authenticate(user)
        return Context(accountId, user, member, accounts, members, activeAccountContextProvider(users, accounts, members))
    }
}
