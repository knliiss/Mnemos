package com.mnemos.event.adapter.out.persistence.playeridentity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "player_identity_projection")
class PlayerIdentityProjectionEntity(
    @Id
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "nickname", nullable = false, length = 64)
    var nickname: String,
    @Column(name = "normalized_nickname", nullable = false, length = 64)
    var normalizedNickname: String,
    @Column(name = "external_version", nullable = false)
    var externalVersion: Long,
    @Column(name = "observed_at", nullable = false)
    var observedAt: Instant,
) {
    fun isNewerThan(
        externalVersion: Long,
        observedAt: Instant,
    ): Boolean =
        this.externalVersion > externalVersion ||
            (this.externalVersion == externalVersion && this.observedAt.isAfter(observedAt))

    fun update(
        nickname: String,
        normalizedNickname: String,
        externalVersion: Long,
        observedAt: Instant,
    ) {
        this.nickname = nickname
        this.normalizedNickname = normalizedNickname
        this.externalVersion = externalVersion
        this.observedAt = observedAt
    }
}
