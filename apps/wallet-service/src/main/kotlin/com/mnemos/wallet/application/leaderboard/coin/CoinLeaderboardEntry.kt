package com.mnemos.wallet.application.leaderboard.coin

import com.mnemos.common.core.model.id.AccountId
import java.util.UUID

data class CoinLeaderboardEntry(
    val accountId: AccountId,
    val playerId: UUID,
    val nickname: String,
    val amount: Long,
)
