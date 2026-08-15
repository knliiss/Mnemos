package com.mnemos.personalization.configuration.revenue

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.personalization.creator")
data class CreatorRevenueProperties(
    val primeSharePercent: Int = 70,
    val coinSharePercent: Int = 50,
    val coinPerPrime: Long = 100,
) {
    init {
        require(primeSharePercent in 1..100) { "Creator PRIME share must be between 1 and 100 percent" }
        require(coinSharePercent in 1..100) { "Creator COIN share must be between 1 and 100 percent" }
        require(coinSharePercent < primeSharePercent) { "COIN creator share must be lower than PRIME creator share" }
        require(coinPerPrime > 0) { "COIN per PRIME conversion must be positive" }
    }
}
