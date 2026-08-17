package com.mnemos.personalization.application.purchase.usecase.purchase

import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface PurchasePersonalizationUseCase {
    fun execute(personalizationId: PersonalizationId): PurchasePersonalizationResult
}
