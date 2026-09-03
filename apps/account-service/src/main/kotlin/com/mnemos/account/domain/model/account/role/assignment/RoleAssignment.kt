package com.mnemos.account.domain.model.account.role.assignment

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import java.time.Instant

class RoleAssignment private constructor(
    val accountId: AccountId,
    val accountMemberId: AccountMemberId,
    val roleId: RoleId,
    val assignedBy: RoleAssignmentActor,
    val reason: RoleAssignmentReason,
    val assignedAt: Instant,
) {
    companion object {
        fun createManual(
            accountId: AccountId,
            accountMemberId: AccountMemberId,
            roleId: RoleId,
            assignedByUserId: UserId,
            now: Instant,
        ): RoleAssignment =
            RoleAssignment(
                accountId = accountId,
                accountMemberId = accountMemberId,
                roleId = roleId,
                assignedBy = RoleAssignmentActor.User(assignedByUserId),
                reason = RoleAssignmentReason.MANUAL,
                assignedAt = now,
            )

        fun createAdministrative(
            accountId: AccountId,
            accountMemberId: AccountMemberId,
            roleId: RoleId,
            now: Instant,
        ): RoleAssignment =
            RoleAssignment(
                accountId = accountId,
                accountMemberId = accountMemberId,
                roleId = roleId,
                assignedBy = RoleAssignmentActor.System,
                reason = RoleAssignmentReason.MANUAL,
                assignedAt = now,
            )

        fun createBaseRole(
            accountId: AccountId,
            accountMemberId: AccountMemberId,
            roleId: RoleId,
            now: Instant,
        ): RoleAssignment =
            RoleAssignment(
                accountId = accountId,
                accountMemberId = accountMemberId,
                roleId = roleId,
                assignedBy = RoleAssignmentActor.System,
                reason = RoleAssignmentReason.BASE_ROLE,
                assignedAt = now,
            )

        fun restore(
            accountId: AccountId,
            accountMemberId: AccountMemberId,
            roleId: RoleId,
            assignedBy: RoleAssignmentActor,
            reason: RoleAssignmentReason,
            assignedAt: Instant,
        ): RoleAssignment =
            RoleAssignment(
                accountId = accountId,
                accountMemberId = accountMemberId,
                roleId = roleId,
                assignedBy = assignedBy,
                reason = reason,
                assignedAt = assignedAt,
            )
    }
}
