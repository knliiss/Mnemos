package com.mnemos.bet.adapter.out.persistence.leaderboard

import java.util.UUID

interface PredictorLeaderboardProjection {
    val accountId: UUID
    val bets: Long
    val wins: Long
    val profit: Long
    val turnover: Long
}
