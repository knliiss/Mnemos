package com.mnemos.accountservice.persistence.mapper

import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignmentActor
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentActorType
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentEntity
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentEntityId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import java.util.UUID

object AccountRoleAssignmentPersistenceMapper {
    fun RoleAssignment.toEntity(): RoleAssignmentEntity {
        val actor = assignedBy.toPersistenceData()

        return RoleAssignmentEntity(
            id =
                RoleAssignmentEntityId(
                    accountMemberId = accountMemberId.value,
                    roleId = roleId.value,
                ),
            accountId = accountId.value,
            assignedByType = actor.type,
            assignedByUserId = actor.userId,
            assignmentReason = reason,
            assignedAt = assignedAt,
        )
    }

    fun RoleAssignmentEntity.toDomain(): RoleAssignment =
        RoleAssignment.restore(
            accountMemberId = AccountMemberId(id.accountMemberId),
            roleId = RoleId(id.roleId),
            accountId = AccountId(accountId),
            assignedBy = toDomainActor(),
            reason = assignmentReason,
            assignedAt = assignedAt,
        )

    private fun RoleAssignmentActor.toPersistenceData(): AssignmentActorData =
        when (this) {
            is RoleAssignmentActor.User ->
                AssignmentActorData(
                    type = RoleAssignmentActorType.USER,
                    userId = userId.value,
                )

            RoleAssignmentActor.System ->
                AssignmentActorData(
                    type = RoleAssignmentActorType.SYSTEM,
                    userId = null,
                )
        }

    private fun RoleAssignmentEntity.toDomainActor(): RoleAssignmentActor =
        when (assignedByType) {
            RoleAssignmentActorType.USER -> {
                val userId =
                    requireNotNull(assignedByUserId) {
                        "USER role assignment must contain assignedByUserId"
                    }

                RoleAssignmentActor.User(UserId(userId))
            }

            RoleAssignmentActorType.SYSTEM -> {
                require(assignedByUserId == null) {
                    "SYSTEM role assignment must not contain assignedByUserId"
                }

                RoleAssignmentActor.System
            }
        }

    private data class AssignmentActorData(
        val type: RoleAssignmentActorType,
        val userId: UUID?,
    )
}
