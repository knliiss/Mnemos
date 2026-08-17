package com.mnemos.personalization.application.purchase.usecase.purchase

import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import java.util.UUID

interface PurchasePersonalizationUseCase {
    fun execute(
        personalizationId: PersonalizationId,
        idempotencyKey: UUID? = null,
    ): PurchasePersonalizationResult
}
