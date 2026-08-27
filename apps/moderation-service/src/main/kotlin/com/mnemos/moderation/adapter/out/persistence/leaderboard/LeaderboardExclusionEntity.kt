package com.mnemos.moderation.adapter.out.persistence.leaderboard

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "leaderboard_exclusions")
class LeaderboardExclusionEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 16)
    val subjectType: LeaderboardExclusionSubjectType,
    @Column(name = "subject_id", nullable = false)
    val subjectId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 16)
    val scope: LeaderboardExclusionScope,
    @Column(name = "reason", length = 256)
    val reason: String?,
    @Column(name = "created_by_user_id", nullable = false)
    val createdByUserId: UUID,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
