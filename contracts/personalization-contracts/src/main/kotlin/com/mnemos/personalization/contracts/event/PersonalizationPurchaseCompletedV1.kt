package com.mnemos.personalization.contracts.event

import com.mnemos.personalization.contracts.shared.PersonalizationPurchaseStatus
import java.time.Instant
import java.util.UUID

data class PersonalizationPurchaseCompletedV1(
    val operationId: UUID,
    val personalizationId: UUID,
    val userId: UUID,
    val status: PersonalizationPurchaseStatus,
    val walletTransactionId: UUID?,
    val failureReason: String?,
    val occurredAt: Instant,
)
