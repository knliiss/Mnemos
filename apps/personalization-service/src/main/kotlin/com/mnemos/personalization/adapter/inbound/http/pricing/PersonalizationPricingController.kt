package com.mnemos.personalization.adapter.inbound.http.pricing

import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationPriceResponse
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/personalizations/pricing")
class PersonalizationPricingController(
    private val pricePolicy: PersonalizationPricePolicy,
) {
    @GetMapping
    fun get(): PersonalizationPricingResponse {
        val customPurchasePrice = pricePolicy.customNotificationPackPurchasePrice()

        return PersonalizationPricingResponse(
            ranges =
                pricePolicy.marketplaceRanges().map { option ->
                    PersonalizationPriceRangeResponse(
                        type = option.type,
                        origin = option.origin,
                        currency = option.currency,
                        min = option.range.min,
                        max = option.range.max,
                    )
                },
            customNotificationPackPurchasePrice =
                PersonalizationPriceResponse(
                    currency = customPurchasePrice.currency,
                    amount = customPurchasePrice.amount,
                ),
        )
    }
}
