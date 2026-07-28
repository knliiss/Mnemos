package com.mnemos.accountservice.persistence.entity.account

import com.mnemos.accountservice.domain.model.account.AccountStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "accounts")
class AccountEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "owner_member_id", nullable = false, updatable = false)
    var ownerMemberId: UUID,
    @Column(name = "cristalix_uuid", nullable = false, unique = true, updatable = false)
    var cristalixUuid: UUID,
    @Column(name = "cristalix_nickname", nullable = false)
    var cristalixNickname: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: AccountStatus,
    @Column(name = "deletion_requested_at")
    var deletionRequestedAt: Instant?,
    @Column(name = "deleted_at")
    var deletedAt: Instant?,
    @Column(name = "max_members", nullable = false)
    var maxMembers: Int,
    @Column(name = "base_role_id")
    var baseRoleId: UUID?,
)
