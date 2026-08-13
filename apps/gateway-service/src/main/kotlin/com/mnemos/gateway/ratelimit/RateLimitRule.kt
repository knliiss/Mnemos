package com.mnemos.gateway.ratelimit

data class RateLimitRule(
    val sustainedPerMinute: Int,
    val burstCapacity: Int,
) {
    init {
        require(sustainedPerMinute > 0)
        require(burstCapacity > 0)
    }

    fun dividedBy(divisor: Int): RateLimitRule {
        require(divisor > 0)

        return RateLimitRule(
            sustainedPerMinute = maxOf(1, sustainedPerMinute / divisor),
            burstCapacity = maxOf(1, burstCapacity / divisor),
        )
    }
}
