package com.mnemos.personalization.adapter.inbound.http.purchase

import java.util.UUID

data class PurchasePersonalizationResponse(
    val personalizationId: UUID,
    val operationId: UUID,
)
