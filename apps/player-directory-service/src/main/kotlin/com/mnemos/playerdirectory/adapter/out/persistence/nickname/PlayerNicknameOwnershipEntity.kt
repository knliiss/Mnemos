package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_nickname_owners")
class PlayerNicknameOwnershipEntity(
    @Id
    @Column(name = "normalized_nickname", nullable = false, length = 64)
    var normalizedNickname: String,
    @Column(name = "nickname", nullable = false, length = 64)
    var nickname: String,
    @Column(name = "player_id", nullable = false, unique = true)
    var playerId: UUID,
    @Column(name = "external_version", nullable = false)
    var externalVersion: Long,
    @Column(name = "observed_at", nullable = false)
    var observedAt: Instant,
)
