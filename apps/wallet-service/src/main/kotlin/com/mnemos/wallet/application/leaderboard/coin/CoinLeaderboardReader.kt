package com.mnemos.wallet.application.leaderboard.coin

interface CoinLeaderboardReader {
    fun read(limit: Int): List<CoinLeaderboardCandidate>
}
