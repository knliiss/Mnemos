package com.mnemos.bet.adapter.inbound.http.leaderboard

import java.math.BigDecimal
import java.util.UUID

data class PredictorLeaderboardResponse(
    val rank: Int,
    val accountId: UUID,
    val nickname: String,
    val bets: Long,
    val wins: Long,
    val winRate: BigDecimal,
    val profit: Long,
    val turnover: Long,
)
