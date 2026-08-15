package com.mnemos.personalization.adapter.inbound.http.personalization.request

import com.mnemos.wallet.contracts.wallet.shared.Currency
import jakarta.validation.constraints.Positive

data class PersonalizationPriceRequest(
    val currency: Currency,
    @field:Positive
    val amount: Long,
)
