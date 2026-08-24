package com.mnemos.wallet.application.leaderboard.coin

import com.mnemos.common.core.model.id.AccountId

data class CoinLeaderboardCandidate(
    val accountId: AccountId,
    val amount: Long,
)
