package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.application.revenue.query.CreatorRevenueSummary
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation

object CreatorRevenueApiMapper {
    fun CreatorRevenueSummary.toResponse(): CreatorRevenueSummaryResponse =
        CreatorRevenueSummaryResponse(
            confirmedPrime = confirmedPrime,
            pendingPrime = pendingPrime,
            failedPrime = failedPrime,
            confirmedOperations = confirmedOperations,
            pendingOperations = pendingOperations,
            failedOperations = failedOperations,
        )

    fun CreatorRevenueOperation.toResponse(): CreatorRevenueOperationResponse =
        CreatorRevenueOperationResponse(
            id = id.id,
            purchaseOperationId = purchaseOperationId.id,
            amountPrime = amountPrime,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            nextRetryAt = nextRetryAt,
            createdAt = createdAt,
        )
}
