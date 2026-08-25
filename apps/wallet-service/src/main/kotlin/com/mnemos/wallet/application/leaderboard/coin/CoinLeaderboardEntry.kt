package com.mnemos.wallet.application.leaderboard.coin

import com.mnemos.common.core.model.id.AccountId

data class CoinLeaderboardEntry(
    val accountId: AccountId,
    val nickname: String,
    val amount: Long,
)
