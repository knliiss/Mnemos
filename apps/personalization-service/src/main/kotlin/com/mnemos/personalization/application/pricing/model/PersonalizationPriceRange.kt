package com.mnemos.personalization.application.pricing.model

import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

data class PersonalizationPriceRange(
    val type: PersonalizationType,
    val origin: PersonalizationOrigin,
    val currency: Currency,
    val range: PriceRange,
)
