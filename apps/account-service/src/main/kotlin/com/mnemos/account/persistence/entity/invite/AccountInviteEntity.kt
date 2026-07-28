package com.mnemos.account.persistence.entity.invite

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "account_invites")
class AccountInviteEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "account_id", nullable = false, updatable = false)
    var accountId: UUID,
    @Column(name = "code", nullable = false, unique = true, updatable = false)
    var code: String,
    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    var createdByUserId: UUID,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Column(name = "expires_at", updatable = false)
    var expiresAt: Instant?,
    @Column(name = "consumed_at")
    var consumedAt: Instant?,
    @Column(name = "revoked_at")
    var revokedAt: Instant?,
)
