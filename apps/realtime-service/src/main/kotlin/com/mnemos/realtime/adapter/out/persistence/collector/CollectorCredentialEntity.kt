package com.mnemos.realtime.adapter.out.persistence.collector

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "collector_credentials")
class CollectorCredentialEntity(
    @Id
    var id: UUID,
    @Column(name = "name", nullable = false, unique = true, length = 64)
    var name: String,
    @Column(name = "owner_user_id")
    var ownerUserId: UUID?,
    @Column(name = "secret_hash", nullable = false, length = 64)
    var secretHash: String,
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
    @Column(name = "revoked_at")
    var revokedAt: Instant?,
)
