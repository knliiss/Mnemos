package com.mnemos.personalization.adapter.inbound.http.pricing

import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationPriceResponse

data class PersonalizationPricingResponse(
    val ranges: List<PersonalizationPriceRangeResponse>,
    val customNotificationPackPurchasePrice: PersonalizationPriceResponse,
)
