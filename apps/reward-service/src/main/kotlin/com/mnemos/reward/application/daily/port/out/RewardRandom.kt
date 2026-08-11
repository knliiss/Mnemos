package com.mnemos.reward.application.daily.port.out

interface RewardRandom {
    fun nextLong(
        fromInclusive: Long,
        toInclusive: Long,
    ): Long

    fun nextDouble(): Double
}
