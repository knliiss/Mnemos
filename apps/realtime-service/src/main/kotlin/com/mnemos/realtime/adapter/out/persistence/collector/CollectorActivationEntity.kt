package com.mnemos.realtime.adapter.out.persistence.collector

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "collector_activations")
class CollectorActivationEntity(
    @Id
    var id: UUID,
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    var tokenHash: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
    @Column(name = "credential_id")
    var credentialId: UUID?,
    @Column(name = "redeemed_at")
    var redeemedAt: Instant?,
)
