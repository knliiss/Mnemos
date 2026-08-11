package com.mnemos.reward.configuration

data class DailyRewardProperties(
    val coinRanges: List<DailyCoinRange> =
        listOf(
            DailyCoinRange(50, 70),
            DailyCoinRange(70, 90),
            DailyCoinRange(90, 120),
            DailyCoinRange(120, 150),
            DailyCoinRange(150, 190),
            DailyCoinRange(190, 240),
            DailyCoinRange(280, 350),
        ),
    val primeBaseChance: Double = 0.001,
    val primeChancePerStreak: Double = 0.0001,
    val primeMaxChance: Double = 0.01,
    val primeAmount: Long = 1,
    val monthlyRestoreLimit: Int = 2,
)
