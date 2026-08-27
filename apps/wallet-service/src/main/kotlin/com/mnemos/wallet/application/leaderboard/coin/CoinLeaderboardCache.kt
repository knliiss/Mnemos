package com.mnemos.wallet.application.leaderboard.coin

interface CoinLeaderboardCache {
    fun get(limit: Int): List<CoinLeaderboardEntry>?

    fun put(
        limit: Int,
        entries: List<CoinLeaderboardEntry>,
    )

    fun clear()
}
