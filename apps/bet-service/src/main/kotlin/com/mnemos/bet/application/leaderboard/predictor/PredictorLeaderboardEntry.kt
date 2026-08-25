package com.mnemos.bet.application.leaderboard.predictor

import com.mnemos.common.core.model.id.AccountId
import java.math.BigDecimal

data class PredictorLeaderboardEntry(
    val accountId: AccountId,
    val nickname: String,
    val bets: Long,
    val wins: Long,
    val winRate: BigDecimal,
    val profit: Long,
    val turnover: Long,
)
