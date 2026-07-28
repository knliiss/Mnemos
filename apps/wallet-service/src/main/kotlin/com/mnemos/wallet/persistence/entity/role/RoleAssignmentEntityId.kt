package com.mnemos.accountservice.persistence.entity.role

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
class RoleAssignmentEntityId(
    @Column(name = "account_member_id", nullable = false, updatable = false)
    var accountMemberId: UUID,
    @Column(name = "role_id", nullable = false, updatable = false)
    var roleId: UUID,
) : Serializable
