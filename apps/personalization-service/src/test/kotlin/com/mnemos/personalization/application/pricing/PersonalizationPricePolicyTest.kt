package com.mnemos.personalization.application.pricing

import com.mnemos.personalization.configuration.pricing.PersonalizationPricingProperties
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PersonalizationPricePolicyTest {
    private val policy = PersonalizationPricePolicy(PersonalizationPricingProperties())

    @Test
    fun `custom notification pack has higher marketplace minimum than regular pack`() {
        val regularRange =
            policy.rangeFor(
                type = PersonalizationType.NOTIFICATION_PACK,
                origin = PersonalizationOrigin.CREATOR,
                currency = Currency.PRIME,
            )
        val customRange =
            policy.rangeFor(
                type = PersonalizationType.NOTIFICATION_PACK,
                origin = PersonalizationOrigin.CUSTOM,
                currency = Currency.PRIME,
            )

        assertEquals(true, customRange.min > regularRange.min)
    }

    @Test
    fun `notification pack rejects coin price`() {
        val price =
            PersonalizationPrice(
                currency = Currency.COIN,
                amount = 1_000,
            )

        assertThrows(IllegalArgumentException::class.java) {
            policy.requireAllowed(
                type = PersonalizationType.NOTIFICATION_PACK,
                origin = PersonalizationOrigin.CREATOR,
                price = price,
            )
        }
    }
}
