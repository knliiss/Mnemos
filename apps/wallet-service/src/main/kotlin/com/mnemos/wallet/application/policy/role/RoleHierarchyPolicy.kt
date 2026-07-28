package com.mnemos.accountservice.application.policy.role

import com.mnemos.accountservice.domain.exception.account.member.AccountMemberKickForbiddenException
import com.mnemos.accountservice.domain.exception.role.RoleHierarchyInconsistentException
import com.mnemos.accountservice.domain.exception.role.RoleHierarchyViolationException
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.domain.model.account.role.RoleId
import org.springframework.stereotype.Component

@Component
class RoleHierarchyPolicy {
    fun requireCanManageRole(
        ownerMemberId: AccountMemberId,
        actorMemberId: AccountMemberId,
        actorHighestRank: Long?,
        roleId: RoleId,
        roleRank: Long,
    ) {
        if (actorMemberId == ownerMemberId) return

        val actorRank =
            actorHighestRank
                ?: throw RoleHierarchyInconsistentException(actorMemberId)

        if (actorRank >= roleRank) {
            throw RoleHierarchyViolationException(
                actorMemberId = actorMemberId,
                roleId = roleId,
            )
        }
    }

    fun requireCanChangeRoleAssignment(
        ownerMemberId: AccountMemberId,
        actorMemberId: AccountMemberId,
        actorHighestRank: Long?,
        subjectMemberId: AccountMemberId,
        subjectHighestRank: Long?,
        roleId: RoleId,
        roleRank: Long,
    ) {
        if (actorMemberId == ownerMemberId) return

        val actorRank =
            actorHighestRank
                ?: throw RoleHierarchyInconsistentException(actorMemberId)

        val cannotManageSubject =
            actorMemberId == subjectMemberId ||
                subjectMemberId == ownerMemberId ||
                subjectHighestRank != null &&
                actorRank >= subjectHighestRank

        val cannotManageRole = actorRank >= roleRank

        if (cannotManageSubject || cannotManageRole) {
            throw RoleHierarchyViolationException(
                actorMemberId = actorMemberId,
                subjectMemberId = subjectMemberId,
                roleId = roleId,
            )
        }
    }

    fun requireCanReorderRoles(
        ownerMemberId: AccountMemberId,
        actorMemberId: AccountMemberId,
        actorHighestRank: Long?,
        currentRoles: List<Role>,
        reorderedRoles: List<Role>,
    ) {
        if (actorMemberId == ownerMemberId) return

        val actorRank =
            actorHighestRank
                ?: throw IllegalStateException(
                    "Non-owner account member must have a highest role rank",
                )

        val currentProtectedRoleIds =
            currentRoles
                .filter { it.hierarchyRank <= actorRank }
                .map { it.id }

        val reorderedProtectedRoleIds =
            reorderedRoles
                .take(currentProtectedRoleIds.size)
                .map { it.id }

        if (currentProtectedRoleIds != reorderedProtectedRoleIds) {
            throw RoleHierarchyViolationException(
                actorMemberId = actorMemberId,
            )
        }
    }

    fun requireCanKickMember(
        ownerMemberId: AccountMemberId,
        actorMemberId: AccountMemberId,
        actorHighestRank: Long?,
        targetMemberId: AccountMemberId,
        targetHighestRank: Long?,
    ) {
        if (
            targetMemberId == actorMemberId ||
            targetMemberId == ownerMemberId
        ) {
            throw AccountMemberKickForbiddenException(
                actorMemberId = actorMemberId,
                targetMemberId = targetMemberId,
            )
        }

        if (actorMemberId == ownerMemberId) return

        val actorRank =
            actorHighestRank
                ?: throw RoleHierarchyInconsistentException(actorMemberId)

        if (targetHighestRank != null && actorRank >= targetHighestRank) {
            throw AccountMemberKickForbiddenException(
                actorMemberId = actorMemberId,
                targetMemberId = targetMemberId,
            )
        }
    }
}
