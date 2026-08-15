package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.wallet.contracts.wallet.shared.Currency

data class PersonalizationPriceResponse(
    val currency: Currency,
    val amount: Long,
)
