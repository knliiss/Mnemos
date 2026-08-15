package com.mnemos.personalization.domain.personalization.model

import com.mnemos.wallet.contracts.wallet.shared.Currency

data class PersonalizationPrice(
    val currency: Currency,
    val amount: Long,
) {
    init {
        require(amount > 0) { "Personalization price must be positive" }
    }
}
