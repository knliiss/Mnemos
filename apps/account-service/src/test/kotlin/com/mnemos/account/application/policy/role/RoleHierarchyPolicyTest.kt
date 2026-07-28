package com.mnemos.account.application.policy.role

import com.mnemos.account.domain.exception.account.member.AccountMemberKickForbiddenException
import com.mnemos.account.domain.exception.role.RoleHierarchyInconsistentException
import com.mnemos.account.domain.exception.role.RoleHierarchyViolationException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.support.role
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class RoleHierarchyPolicyTest {
    private val policy = RoleHierarchyPolicy()

    @Test
    fun `owner can manage any role`() {
        val owner = memberId()
        policy.requireCanManageRole(owner, owner, null, roleId(), 1)
    }

    @Test
    fun `non-owner requires assigned role`() {
        assertThrows<RoleHierarchyInconsistentException> {
            policy.requireCanManageRole(memberId(), memberId(), null, roleId(), Role.RANK_STEP)
        }
    }

    @Test
    fun `non-owner can manage only lower role`() {
        val owner = memberId()
        val actor = memberId()
        policy.requireCanManageRole(owner, actor, Role.RANK_STEP, roleId(), 2 * Role.RANK_STEP)
        assertThrows<RoleHierarchyViolationException> {
            policy.requireCanManageRole(owner, actor, Role.RANK_STEP, roleId(), Role.RANK_STEP)
        }
    }

    @Test
    fun `role assignment rejects self owner and higher subject`() {
        val owner = memberId()
        val actor = memberId()
        val roleId = roleId()

        assertThrows<RoleHierarchyViolationException> {
            policy.requireCanChangeRoleAssignment(owner, actor, Role.RANK_STEP, actor, null, roleId, 2 * Role.RANK_STEP)
        }
        assertThrows<RoleHierarchyViolationException> {
            policy.requireCanChangeRoleAssignment(owner, actor, Role.RANK_STEP, owner, null, roleId, 2 * Role.RANK_STEP)
        }
        assertThrows<RoleHierarchyViolationException> {
            policy.requireCanChangeRoleAssignment(owner, actor, 2 * Role.RANK_STEP, memberId(), Role.RANK_STEP, roleId, 3 * Role.RANK_STEP)
        }
    }

    @Test
    fun `role assignment rejects role not below actor`() {
        assertThrows<RoleHierarchyViolationException> {
            policy.requireCanChangeRoleAssignment(
                memberId(),
                memberId(),
                Role.RANK_STEP,
                memberId(),
                null,
                roleId(),
                Role.RANK_STEP,
            )
        }
    }

    @Test
    fun `non-owner can reorder only unprotected lower roles`() {
        val owner = memberId()
        val actor = memberId()
        val first = role(rank = Role.RANK_STEP)
        val actorRole = role(rank = 2 * Role.RANK_STEP)
        val lowerOne = role(rank = 3 * Role.RANK_STEP)
        val lowerTwo = role(rank = 4 * Role.RANK_STEP)

        policy.requireCanReorderRoles(
            owner,
            actor,
            actorRole.hierarchyRank,
            listOf(first, actorRole, lowerOne, lowerTwo),
            listOf(first, actorRole, lowerTwo, lowerOne),
        )
    }

    @Test
    fun `kick rejects self and owner before owner bypass`() {
        val owner = memberId()
        assertThrows<AccountMemberKickForbiddenException> {
            policy.requireCanKickMember(owner, owner, null, owner, null)
        }
        val actor = memberId()
        assertThrows<AccountMemberKickForbiddenException> {
            policy.requireCanKickMember(owner, actor, Role.RANK_STEP, owner, null)
        }
    }

    @Test
    fun `non-owner can kick only lower member`() {
        val owner = memberId()
        val actor = memberId()
        policy.requireCanKickMember(owner, actor, Role.RANK_STEP, memberId(), 2 * Role.RANK_STEP)
        assertThrows<AccountMemberKickForbiddenException> {
            policy.requireCanKickMember(owner, actor, 2 * Role.RANK_STEP, memberId(), Role.RANK_STEP)
        }
    }

    private fun memberId() = AccountMemberId(UUID.randomUUID())

    private fun roleId() = RoleId(UUID.randomUUID())
}
