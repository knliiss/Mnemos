package com.mnemos.personalization.application.revenue

import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.stereotype.Component

@Component
class CreatorRevenuePolicy(
    private val properties: CreatorRevenueProperties,
) {
    fun calculatePrime(operation: PurchaseOperation): Long =
        when (operation.currency) {
            Currency.PRIME ->
                percentage(operation.amount, properties.primeSharePercent)
                    .coerceAtLeast(1)

            Currency.COIN -> {
                val sharedCoin = percentage(operation.amount, properties.coinSharePercent)

                (sharedCoin / properties.coinPerPrime).coerceAtLeast(1)
            }
        }

    private fun percentage(
        amount: Long,
        percent: Int,
    ): Long {
        val wholeHundreds = amount / 100
        val remainder = amount % 100

        return wholeHundreds * percent + remainder * percent / 100
    }
}
