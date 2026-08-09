package com.mnemos.account.application.policy.role

import com.mnemos.account.application.context.account.ActiveAccountContext
import com.mnemos.account.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.account.domain.exception.role.InvalidRoleHierarchyException
import com.mnemos.account.domain.exception.role.RoleAccessDeniedException
import com.mnemos.account.domain.exception.role.RoleAccessDeniedReason
import com.mnemos.account.domain.exception.role.RoleNotFoundException
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Component

@Component
class RoleAccessGuard(
    private val roleRepository: RoleRepository,
    private val accountMemberRepository: AccountMemberRepository,
) {
    fun requireCanManageRole(
        context: ActiveAccountContext,
        roleId: RoleId,
    ): Role {
        val role =
            requireRole(
                accountId = context.account.id,
                roleId = roleId,
            )

        requireCanManageRole(
            context = context,
            role = role,
        )

        return role
    }

    fun requireCanManageRole(
        context: ActiveAccountContext,
        role: Role,
    ) {
        if (context.account.isOwner(context.member.id)) {
            return
        }

        val actorHighestRole = requireActorHighestRole(context)

        requireStrictlyHigher(
            context = context,
            actorRole = actorHighestRole,
            targetRole = role,
            reason = RoleAccessDeniedReason.ROLE_NOT_LOWER,
        )
    }

    fun requireCanChangeRoleAssignment(
        context: ActiveAccountContext,
        targetMemberId: AccountMemberId,
        roleId: RoleId,
    ): RoleAssignmentAccess {
        val targetMember =
            requireMember(
                accountId = context.account.id,
                memberId = targetMemberId,
            )

        val role =
            requireRole(
                accountId = context.account.id,
                roleId = roleId,
            )

        if (context.account.isOwner(context.member.id)) {
            return RoleAssignmentAccess(
                targetMember = targetMember,
                role = role,
            )
        }

        if (targetMember.id == context.member.id) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.SELF_ROLE_MANAGEMENT,
            )
        }

        if (context.account.isOwner(targetMember.id)) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.TARGET_IS_OWNER,
            )
        }

        val actorHighestRole = requireActorHighestRole(context)

        requireStrictlyHigher(
            context = context,
            actorRole = actorHighestRole,
            targetRole = role,
            reason = RoleAccessDeniedReason.ROLE_NOT_LOWER,
        )

        val targetHighestRole =
            findHighestRole(
                accountId = context.account.id,
                memberId = targetMember.id,
            )

        if (targetHighestRole != null) {
            requireStrictlyHigher(
                context = context,
                actorRole = actorHighestRole,
                targetRole = targetHighestRole,
                reason = RoleAccessDeniedReason.TARGET_MEMBER_NOT_LOWER,
            )
        }

        return RoleAssignmentAccess(
            targetMember = targetMember,
            role = role,
        )
    }

    fun requireCanKickMember(
        context: ActiveAccountContext,
        targetMemberId: AccountMemberId,
    ): AccountMember {
        val targetMember =
            requireMember(
                accountId = context.account.id,
                memberId = targetMemberId,
            )

        if (targetMember.id == context.member.id) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.SELF_KICK,
            )
        }

        if (context.account.isOwner(targetMember.id)) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.TARGET_IS_OWNER,
            )
        }

        if (context.account.isOwner(context.member.id)) {
            return targetMember
        }

        val actorHighestRole = requireActorHighestRole(context)

        val targetHighestRole =
            findHighestRole(
                accountId = context.account.id,
                memberId = targetMember.id,
            )

        if (
            targetHighestRole != null &&
            actorHighestRole.hierarchyRank >= targetHighestRole.hierarchyRank
        ) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.TARGET_MEMBER_NOT_LOWER,
            )
        }

        return targetMember
    }

    fun requireCanUseAsBaseRole(
        context: ActiveAccountContext,
        roleId: RoleId,
    ): Role =
        requireCanManageRole(
            context = context,
            roleId = roleId,
        )

    fun requireCanUpdateHierarchy(
        context: ActiveAccountContext,
        roles: List<Role>,
        orderedRoleIds: List<RoleId>,
    ) {
        requireValidRoleSet(
            accountId = context.account.id,
            roles = roles,
            orderedRoleIds = orderedRoleIds,
        )

        if (context.account.isOwner(context.member.id)) {
            return
        }

        val actorHighestRole = requireActorHighestRole(context)

        val currentOrder =
            roles
                .sortedBy(Role::hierarchyRank)
                .map(Role::id)

        val protectedRoleIds =
            roles
                .filter { it.hierarchyRank <= actorHighestRole.hierarchyRank }
                .sortedBy(Role::hierarchyRank)
                .map(Role::id)

        val newProtectedOrder = orderedRoleIds.take(protectedRoleIds.size)

        if (newProtectedOrder != protectedRoleIds) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.PROTECTED_HIERARCHY_CHANGED,
            )
        }

        if (currentOrder.indexOf(actorHighestRole.id) != orderedRoleIds.indexOf(actorHighestRole.id)) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.OWN_ROLE_POSITION_CHANGED,
            )
        }
    }

    private fun requireActorHighestRole(context: ActiveAccountContext): Role =
        findHighestRole(
            accountId = context.account.id,
            memberId = context.member.id,
        ) ?: throw RoleAccessDeniedException(
            accountId = context.account.id,
            reason = RoleAccessDeniedReason.ACTOR_HAS_NO_ROLE,
        )

    private fun requireStrictlyHigher(
        context: ActiveAccountContext,
        actorRole: Role,
        targetRole: Role,
        reason: RoleAccessDeniedReason,
    ) {
        if (actorRole.hierarchyRank >= targetRole.hierarchyRank) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = reason,
            )
        }
    }

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
    ): AccountMember =
        accountMemberRepository.findByIdAndAccountId(
            memberId = memberId,
            accountId = accountId,
        ) ?: throw AccountMemberNotFoundException(
            accountId = accountId,
            memberId = memberId,
        )

    private fun findHighestRole(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): Role? =
        roleRepository
            .findAllByMemberId(
                accountId = accountId,
                memberId = memberId,
            ).minByOrNull(Role::hierarchyRank)

    private fun requireValidRoleSet(
        accountId: AccountId,
        roles: List<Role>,
        orderedRoleIds: List<RoleId>,
    ) {
        if (orderedRoleIds.size != orderedRoleIds.toSet().size) {
            throw InvalidRoleHierarchyException(
                accountId = accountId,
                reason = "DUPLICATE_ROLE_IDS",
            )
        }

        val existingRoleIds = roles.mapTo(mutableSetOf(), Role::id)
        val requestedRoleIds = orderedRoleIds.toSet()

        if (existingRoleIds != requestedRoleIds) {
            throw InvalidRoleHierarchyException(
                accountId = accountId,
                reason = "ROLE_SET_MISMATCH",
            )
        }
    }
}

data class RoleAssignmentAccess(
    val targetMember: AccountMember,
    val role: Role,
)
