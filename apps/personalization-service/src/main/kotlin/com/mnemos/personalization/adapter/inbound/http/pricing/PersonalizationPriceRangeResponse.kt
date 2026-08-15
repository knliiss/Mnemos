package com.mnemos.personalization.adapter.inbound.http.pricing

import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

data class PersonalizationPriceRangeResponse(
    val type: PersonalizationType,
    val origin: PersonalizationOrigin,
    val currency: Currency,
    val min: Long,
    val max: Long,
)
