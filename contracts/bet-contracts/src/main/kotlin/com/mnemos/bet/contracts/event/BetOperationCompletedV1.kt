package com.mnemos.bet.contracts.event

import com.mnemos.bet.contracts.shared.BetOperationResourceType
import com.mnemos.bet.contracts.shared.BetOperationStatus
import java.time.Instant
import java.util.UUID

data class BetOperationCompletedV1(
    val operationId: UUID,
    val userId: UUID,
    val resourceType: BetOperationResourceType,
    val resourceId: UUID,
    val status: BetOperationStatus,
    val amount: Long,
    val walletTransactionId: UUID?,
    val failureReason: String?,
    val occurredAt: Instant,
)
