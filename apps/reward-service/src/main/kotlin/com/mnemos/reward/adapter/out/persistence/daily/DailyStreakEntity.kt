package com.mnemos.reward.adapter.out.persistence.daily

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "daily_streaks")
class DailyStreakEntity(
    @Id
    @Column(name = "account_id")
    var accountId: UUID,
    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int,
    @Column(name = "last_claimed_date")
    var lastClaimedDate: LocalDate?,
    @Column(name = "recoverable_streak", nullable = false)
    var recoverableStreak: Int,
    @Column(name = "restores_used", nullable = false)
    var restoresUsed: Int,
    @Column(name = "restores_month", nullable = false, length = 7)
    var restoresMonth: String,
)
