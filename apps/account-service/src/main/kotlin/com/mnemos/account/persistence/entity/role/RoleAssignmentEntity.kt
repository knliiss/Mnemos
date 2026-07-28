package com.mnemos.account.persistence.entity.role

import com.mnemos.account.domain.model.account.role.assignment.RoleAssignmentReason
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "role_assignments")
class RoleAssignmentEntity(
    @EmbeddedId
    var id: RoleAssignmentEntityId,
    @Column(name = "account_id", nullable = false)
    var accountId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_by_type", nullable = false)
    var assignedByType: RoleAssignmentActorType,
    @Column(name = "assigned_by_user_id")
    var assignedByUserId: UUID?,
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_reason", nullable = false)
    var assignmentReason: RoleAssignmentReason,
    @Column(name = "assigned_at", nullable = false)
    var assignedAt: Instant,
)
