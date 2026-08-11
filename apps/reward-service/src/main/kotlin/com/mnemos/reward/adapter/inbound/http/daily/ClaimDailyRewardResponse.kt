package com.mnemos.reward.adapter.inbound.http.daily

data class ClaimDailyRewardResponse(
    val streak: Int,
    val coins: Long,
    val primes: Long,
    val primeChance: Double,
)
