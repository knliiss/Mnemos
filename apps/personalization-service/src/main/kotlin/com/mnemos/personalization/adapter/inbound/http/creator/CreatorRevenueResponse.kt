package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import java.time.Instant
import java.util.UUID

data class CreatorRevenueSummaryResponse(
    val confirmedPrime: Long,
    val pendingPrime: Long,
    val failedPrime: Long,
    val confirmedOperations: Int,
    val pendingOperations: Int,
    val failedOperations: Int,
)

data class CreatorRevenueOperationResponse(
    val id: UUID,
    val purchaseOperationId: UUID,
    val amountPrime: Long,
    val status: CreatorRevenueOperationStatus,
    val walletTransactionId: UUID?,
    val failureReason: String?,
    val nextRetryAt: Instant?,
    val createdAt: Instant,
)
