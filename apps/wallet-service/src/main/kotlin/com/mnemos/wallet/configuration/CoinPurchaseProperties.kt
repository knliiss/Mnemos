package com.mnemos.wallet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("mnemos.wallet.coin-purchase")
class CoinPurchaseProperties {
    var coinPerPrime: Long = 100
    var primeAmounts: List<Long> = listOf(5, 10, 25, 50, 100, 250)

    fun normalizedPrimeAmounts(): List<Long> =
        primeAmounts
            .filter { it > 0 }
            .distinct()
            .sorted()
}
