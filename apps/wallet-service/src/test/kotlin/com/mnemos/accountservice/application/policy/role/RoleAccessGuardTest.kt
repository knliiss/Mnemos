package com.mnemos.accountservice.application.policy.role

import com.mnemos.accountservice.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.accountservice.domain.exception.account.role.RoleAccessDeniedException
import com.mnemos.accountservice.domain.exception.role.InvalidRoleHierarchyException
import com.mnemos.accountservice.domain.exception.role.RoleAccessDeniedReason
import com.mnemos.accountservice.domain.exception.role.RoleNotFoundException
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.support.InMemoryAccountMemberRepository
import com.mnemos.accountservice.support.InMemoryRoleRepository
import com.mnemos.accountservice.support.activeContext
import com.mnemos.accountservice.support.member
import com.mnemos.accountservice.support.role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class RoleAccessGuardTest {
    @Test
    fun `owner can manage any role without assigned roles`() {
        val fixture = activeContext(owner = true)
        val targetRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val guard = guard(listOf(targetRole), listOf(fixture.member))

        assertSame(targetRole, guard.requireCanManageRole(fixture.context, targetRole.id))
    }

    @Test
    fun `non-owner can manage strictly lower role`() {
        val fixture = activeContext()
        val actorRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val targetRole = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val repositories = repositories(listOf(actorRole, targetRole), listOf(fixture.member))
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actorRole.id)

        val result =
            RoleAccessGuard(repositories.roles, repositories.members)
                .requireCanManageRole(fixture.context, targetRole.id)

        assertSame(targetRole, result)
    }

    @Test
    fun `non-owner cannot manage equal or higher role`() {
        listOf(Role.RANK_STEP, 500_000L).forEach { targetRank ->
            val fixture = activeContext()
            val actorRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
            val targetRole = role(accountId = fixture.account.id, rank = targetRank)
            val repositories = repositories(listOf(actorRole, targetRole), listOf(fixture.member))
            repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actorRole.id)
            val exception =
                assertThrows<RoleAccessDeniedException> {
                    RoleAccessGuard(repositories.roles, repositories.members).requireCanManageRole(fixture.context, targetRole.id)
                }
            assertEquals(RoleAccessDeniedReason.ROLE_NOT_LOWER.name, exception.details["reason"])
        }
    }

    @Test
    fun `non-owner without roles cannot manage role`() {
        val fixture = activeContext()
        val targetRole = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val guard = guard(listOf(targetRole), listOf(fixture.member))

        val exception =
            assertThrows<RoleAccessDeniedException> {
                guard.requireCanManageRole(fixture.context, targetRole.id)
            }
        assertEquals(RoleAccessDeniedReason.ACTOR_HAS_NO_ROLE.name, exception.details["reason"])
    }

    @Test
    fun `role lookup is scoped to active account`() {
        val fixture = activeContext(owner = true)
        val foreignRole = role()
        val guard = guard(listOf(foreignRole), listOf(fixture.member))

        assertThrows<RoleNotFoundException> {
            guard.requireCanManageRole(fixture.context, foreignRole.id)
        }
    }

    @Test
    fun `owner can assign role to any existing member`() {
        val fixture = activeContext(owner = true)
        val targetMember = member(accountId = fixture.account.id)
        val targetRole = role(accountId = fixture.account.id)
        val guard = guard(listOf(targetRole), listOf(fixture.member, targetMember))

        val access = guard.requireCanChangeRoleAssignment(fixture.context, targetMember.id, targetRole.id)

        assertSame(targetMember, access.targetMember)
        assertSame(targetRole, access.role)
    }

    @Test
    fun `role assignment rejects missing target member`() {
        val fixture = activeContext(owner = true)
        val targetRole = role(accountId = fixture.account.id)
        val guard = guard(listOf(targetRole), listOf(fixture.member))

        assertThrows<AccountMemberNotFoundException> {
            guard.requireCanChangeRoleAssignment(fixture.context, AccountMemberId(UUID.randomUUID()), targetRole.id)
        }
    }

    @Test
    fun `non-owner cannot manage own roles`() {
        val fixture = activeContext()
        val actorRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val targetRole = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val repositories = repositories(listOf(actorRole, targetRole), listOf(fixture.member))
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actorRole.id)

        val exception =
            assertThrows<RoleAccessDeniedException> {
                RoleAccessGuard(repositories.roles, repositories.members)
                    .requireCanChangeRoleAssignment(fixture.context, fixture.member.id, targetRole.id)
            }
        assertEquals(RoleAccessDeniedReason.SELF_ROLE_MANAGEMENT.name, exception.details["reason"])
    }

    @Test
    fun `non-owner cannot manage owner roles`() {
        val fixture = activeContext()
        val ownerMember = member(id = fixture.account.ownerMemberId, accountId = fixture.account.id)
        val actorRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val targetRole = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val repositories = repositories(listOf(actorRole, targetRole), listOf(fixture.member, ownerMember))
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actorRole.id)

        val exception =
            assertThrows<RoleAccessDeniedException> {
                RoleAccessGuard(repositories.roles, repositories.members)
                    .requireCanChangeRoleAssignment(fixture.context, ownerMember.id, targetRole.id)
            }
        assertEquals(RoleAccessDeniedReason.TARGET_IS_OWNER.name, exception.details["reason"])
    }

    @Test
    fun `non-owner can change lower member lower role`() {
        val fixture = activeContext()
        val targetMember = member(accountId = fixture.account.id)
        val actorRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val targetHighestRole = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val roleToAssign = role(accountId = fixture.account.id, rank = 3 * Role.RANK_STEP)
        val repositories =
            repositories(
                listOf(actorRole, targetHighestRole, roleToAssign),
                listOf(fixture.member, targetMember),
            )
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actorRole.id)
        repositories.roles.memberRoles[targetMember.id] = mutableSetOf(targetHighestRole.id)

        val access =
            RoleAccessGuard(repositories.roles, repositories.members)
                .requireCanChangeRoleAssignment(fixture.context, targetMember.id, roleToAssign.id)

        assertSame(targetMember, access.targetMember)
    }

    @Test
    fun `non-owner cannot change role of equal or higher member`() {
        val fixture = activeContext()
        val targetMember = member(accountId = fixture.account.id)
        val actorRole = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val targetHighestRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val roleToAssign = role(accountId = fixture.account.id, rank = 3 * Role.RANK_STEP)
        val repositories =
            repositories(
                listOf(actorRole, targetHighestRole, roleToAssign),
                listOf(fixture.member, targetMember),
            )
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actorRole.id)
        repositories.roles.memberRoles[targetMember.id] = mutableSetOf(targetHighestRole.id)

        val exception =
            assertThrows<RoleAccessDeniedException> {
                RoleAccessGuard(repositories.roles, repositories.members)
                    .requireCanChangeRoleAssignment(fixture.context, targetMember.id, roleToAssign.id)
            }
        assertEquals(RoleAccessDeniedReason.TARGET_MEMBER_NOT_LOWER.name, exception.details["reason"])
    }

    @Test
    fun `kick rejects self and owner`() {
        val fixture = activeContext()
        val owner = member(id = fixture.account.ownerMemberId, accountId = fixture.account.id)
        val guard = guard(emptyList(), listOf(fixture.member, owner))

        val self =
            assertThrows<RoleAccessDeniedException> {
                guard.requireCanKickMember(fixture.context, fixture.member.id)
            }
        assertEquals(RoleAccessDeniedReason.SELF_KICK.name, self.details["reason"])

        val ownerException =
            assertThrows<RoleAccessDeniedException> {
                guard.requireCanKickMember(fixture.context, owner.id)
            }
        assertEquals(RoleAccessDeniedReason.TARGET_IS_OWNER.name, ownerException.details["reason"])
    }

    @Test
    fun `owner can kick non-owner regardless of roles`() {
        val fixture = activeContext(owner = true)
        val target = member(accountId = fixture.account.id)
        val guard = guard(emptyList(), listOf(fixture.member, target))

        assertSame(target, guard.requireCanKickMember(fixture.context, target.id))
    }

    @Test
    fun `non-owner can kick only strictly lower member`() {
        val fixture = activeContext()
        val target = member(accountId = fixture.account.id)
        val actorRole = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val targetRole = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val repositories = repositories(listOf(actorRole, targetRole), listOf(fixture.member, target))
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actorRole.id)
        repositories.roles.memberRoles[target.id] = mutableSetOf(targetRole.id)

        assertSame(
            target,
            RoleAccessGuard(repositories.roles, repositories.members).requireCanKickMember(fixture.context, target.id),
        )
    }

    @Test
    fun `hierarchy update validates role set before access rules`() {
        val fixture = activeContext(owner = true)
        val first = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val second = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val guard = guard(listOf(first, second), listOf(fixture.member))

        assertThrows<InvalidRoleHierarchyException> {
            guard.requireCanUpdateHierarchy(fixture.context, listOf(first, second), listOf(first.id, first.id))
        }
        assertThrows<InvalidRoleHierarchyException> {
            guard.requireCanUpdateHierarchy(fixture.context, listOf(first, second), listOf(first.id))
        }
    }

    @Test
    fun `non-owner may reorder only roles below own role`() {
        val fixture = activeContext()
        val higher = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val actor = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val lowerOne = role(accountId = fixture.account.id, rank = 3 * Role.RANK_STEP)
        val lowerTwo = role(accountId = fixture.account.id, rank = 4 * Role.RANK_STEP)
        val all = listOf(higher, actor, lowerOne, lowerTwo)
        val repositories = repositories(all, listOf(fixture.member))
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actor.id)
        val guard = RoleAccessGuard(repositories.roles, repositories.members)

        guard.requireCanUpdateHierarchy(
            fixture.context,
            all,
            listOf(higher.id, actor.id, lowerTwo.id, lowerOne.id),
        )
    }

    @Test
    fun `non-owner cannot alter protected hierarchy or own position`() {
        val fixture = activeContext()
        val higher = role(accountId = fixture.account.id, rank = Role.RANK_STEP)
        val actor = role(accountId = fixture.account.id, rank = 2 * Role.RANK_STEP)
        val lower = role(accountId = fixture.account.id, rank = 3 * Role.RANK_STEP)
        val all = listOf(higher, actor, lower)
        val repositories = repositories(all, listOf(fixture.member))
        repositories.roles.memberRoles[fixture.member.id] = mutableSetOf(actor.id)
        val guard = RoleAccessGuard(repositories.roles, repositories.members)

        val protected =
            assertThrows<RoleAccessDeniedException> {
                guard.requireCanUpdateHierarchy(fixture.context, all, listOf(actor.id, higher.id, lower.id))
            }
        assertEquals(RoleAccessDeniedReason.PROTECTED_HIERARCHY_CHANGED.name, protected.details["reason"])

        val ownPosition =
            assertThrows<RoleAccessDeniedException> {
                guard.requireCanUpdateHierarchy(fixture.context, all, listOf(higher.id, lower.id, actor.id))
            }
        assertEquals(RoleAccessDeniedReason.PROTECTED_HIERARCHY_CHANGED.name, ownPosition.details["reason"])
    }

    private data class Repositories(
        val roles: InMemoryRoleRepository,
        val members: InMemoryAccountMemberRepository,
    )

    private fun repositories(
        roles: List<com.mnemos.accountservice.domain.model.account.role.Role>,
        members: List<com.mnemos.accountservice.domain.model.account.AccountMember>,
    ) = Repositories(InMemoryRoleRepository(roles), InMemoryAccountMemberRepository(members))

    private fun guard(
        roles: List<com.mnemos.accountservice.domain.model.account.role.Role>,
        members: List<com.mnemos.accountservice.domain.model.account.AccountMember>,
    ): RoleAccessGuard {
        val repositories = repositories(roles, members)
        return RoleAccessGuard(repositories.roles, repositories.members)
    }
}
