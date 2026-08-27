package com.mnemos.playerdirectory.adapter.out.persistence.player

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "players")
class PlayerEntity(
    @Id
    @Column(name = "player_id", nullable = false)
    var playerId: UUID,
    @Column(name = "current_nickname", nullable = false, length = 64)
    var currentNickname: String,
    @Column(name = "normalized_nickname", nullable = false, length = 64)
    var normalizedNickname: String,
    @Column(name = "external_version", nullable = false)
    var externalVersion: Long,
    @Column(name = "external_updated_at")
    var externalUpdatedAt: Instant?,
    @Column(name = "presence_status", nullable = false, length = 16)
    var presenceStatus: String,
    @Column(name = "last_verified_presence_status", length = 16)
    var lastVerifiedPresenceStatus: String?,
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
    @Column(name = "offline_candidate_since")
    var offlineCandidateSince: Instant?,
    @Column(name = "offline_candidate_count", nullable = false)
    var offlineCandidateCount: Int,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
