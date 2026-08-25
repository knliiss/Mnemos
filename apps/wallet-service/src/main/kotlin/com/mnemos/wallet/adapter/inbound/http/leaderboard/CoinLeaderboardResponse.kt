package com.mnemos.wallet.adapter.inbound.http.leaderboard

import java.util.UUID

data class CoinLeaderboardResponse(
    val rank: Int,
    val accountId: UUID,
    val nickname: String,
    val amount: Long,
)
