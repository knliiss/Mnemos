package com.mnemos.personalization.adapter.inbound.http.purchase

import com.mnemos.personalization.domain.purchase.model.PurchaseOperation

object PurchaseOperationApiMapper {
    fun PurchaseOperation.toResponse(): PurchaseOperationResponse =
        PurchaseOperationResponse(
            operationId = id.id,
            personalizationId = personalizationId.id,
            ownerType = owner.target,
            ownerId = owner.id,
            currency = currency,
            amount = amount,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            createdAt = createdAt,
        )
}
