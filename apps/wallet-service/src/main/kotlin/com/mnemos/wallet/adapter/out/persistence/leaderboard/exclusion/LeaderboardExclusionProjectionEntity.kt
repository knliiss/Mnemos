package com.mnemos.wallet.adapter.out.persistence.leaderboard.exclusion

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "leaderboard_exclusion_projection")
@IdClass(LeaderboardExclusionProjectionId::class)
class LeaderboardExclusionProjectionEntity(
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 16)
    val subjectType: LeaderboardExclusionSubjectType,
    @Id
    @Column(name = "subject_id", nullable = false)
    val subjectId: UUID,
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 16)
    val scope: LeaderboardExclusionScope,
    @Column(name = "exclusion_id")
    val exclusionId: UUID?,
    @Column(name = "active", nullable = false)
    val active: Boolean,
    @Column(name = "occurred_at", nullable = false)
    val occurredAt: Instant,
)
