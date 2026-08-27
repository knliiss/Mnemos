package com.mnemos.playerdirectory.adapter.out.persistence.accountlink

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_account_links")
class PlayerAccountLinkEntity(
    @Id
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "account_id", nullable = false, unique = true)
    var accountId: UUID,
    @Column(name = "recorded_at", nullable = false)
    var recordedAt: Instant,
)
