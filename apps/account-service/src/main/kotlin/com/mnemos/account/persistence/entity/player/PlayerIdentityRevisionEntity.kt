package com.mnemos.account.persistence.entity.player

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_identity_revisions")
class PlayerIdentityRevisionEntity(
    @Id
    @Column(name = "player_id", nullable = false, updatable = false)
    var playerId: UUID,
    @Column(name = "nickname", nullable = false)
    var nickname: String,
    @Column(name = "external_version", nullable = false)
    var externalVersion: Long,
    @Column(name = "observed_at", nullable = false)
    var observedAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
