package com.mnemos.bet.application.leaderboard.predictor

import com.mnemos.common.core.model.id.AccountId

data class PredictorLeaderboardCandidate(
    val accountId: AccountId,
    val bets: Long,
    val wins: Long,
    val profit: Long,
    val turnover: Long,
)
