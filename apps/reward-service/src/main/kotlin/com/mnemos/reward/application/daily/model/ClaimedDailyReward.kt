package com.mnemos.reward.application.daily.model

data class ClaimedDailyReward(
    val streak: Int,
    val coins: Long,
    val primes: Long,
    val primeChance: Double,
)
