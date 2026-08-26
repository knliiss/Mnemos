package com.mnemos.profile.adapter.out.persistence.playerstate

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "profile_player_states")
class ProfilePlayerStateEntity(
    @Id
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "nickname", nullable = false, length = 64)
    var nickname: String,
    @Column(name = "external_version", nullable = false)
    var externalVersion: Long,
    @Column(name = "presence_status", nullable = false, length = 16)
    var presenceStatus: String,
    @Column(name = "realm", length = 64)
    var realm: String?,
    @Column(name = "realm_type", length = 64)
    var realmType: String?,
    @Column(name = "privacy_hidden", nullable = false)
    var privacyHidden: Boolean,
    @Column(name = "last_seen_online")
    var lastSeenOnline: Instant?,
    @Column(name = "presence_observed_at", nullable = false)
    var presenceObservedAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
