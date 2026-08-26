package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_nickname_history")
class PlayerNicknameHistoryEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "nickname", nullable = false, length = 64)
    var nickname: String,
    @Column(name = "normalized_nickname", nullable = false, length = 64)
    var normalizedNickname: String,
    @Column(name = "external_version", nullable = false)
    var externalVersion: Long,
    @Column(name = "valid_from", nullable = false)
    var validFrom: Instant,
    @Column(name = "valid_until")
    var validUntil: Instant?,
)
