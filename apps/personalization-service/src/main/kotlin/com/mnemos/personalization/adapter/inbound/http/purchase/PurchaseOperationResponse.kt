package com.mnemos.personalization.adapter.inbound.http.purchase

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.wallet.contracts.wallet.shared.Currency
import java.time.Instant
import java.util.UUID

data class PurchaseOperationResponse(
    val operationId: UUID,
    val personalizationId: UUID,
    val ownerType: PersonalizationTarget,
    val ownerId: UUID,
    val currency: Currency,
    val amount: Long,
    val status: PurchaseOperationStatus,
    val walletTransactionId: UUID?,
    val failureReason: String?,
    val createdAt: Instant,
)
