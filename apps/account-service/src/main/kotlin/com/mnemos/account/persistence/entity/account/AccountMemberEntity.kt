package com.mnemos.account.persistence.entity.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "account_members")
class AccountMemberEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "account_id", nullable = false, updatable = false)
    var accountId: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID,
    @Column(name = "joined_at", nullable = false)
    var joinedAt: Instant,
)
