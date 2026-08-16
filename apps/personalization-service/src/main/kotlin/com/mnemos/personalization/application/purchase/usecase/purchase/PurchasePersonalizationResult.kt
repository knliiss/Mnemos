package com.mnemos.personalization.application.purchase.usecase.purchase

import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId

data class PurchasePersonalizationResult(
    val personalizationId: PersonalizationId,
    val operationId: PurchaseOperationId,
)
