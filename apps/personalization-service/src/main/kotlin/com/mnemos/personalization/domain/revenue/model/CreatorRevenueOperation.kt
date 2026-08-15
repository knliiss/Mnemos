package com.mnemos.personalization.domain.revenue.model

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import java.time.Instant
import java.util.UUID

class CreatorRevenueOperation(
    val id: CreatorRevenueOperationId,
    requestId: UUID,
    val purchaseOperationId: PurchaseOperationId,
    val creatorUserId: UserId,
    val amountPrime: Long,
    val createdAt: Instant,
    status: CreatorRevenueOperationStatus,
    walletTransactionId: UUID?,
    failureReason: String?,
    nextRetryAt: Instant?,
) {
    var requestId: UUID = requestId
        private set
    var status: CreatorRevenueOperationStatus = status
        private set
    var walletTransactionId: UUID? = walletTransactionId
        private set
    var failureReason: String? = failureReason
        private set
    var nextRetryAt: Instant? = nextRetryAt
        private set

    init {
        require(amountPrime > 0) { "Creator revenue must be positive" }
        require((status == CreatorRevenueOperationStatus.FAILED) == (nextRetryAt != null)) {
            "Only failed creator revenue can have a retry time"
        }
    }

    fun complete(transactionId: UUID) {
        require(status == CreatorRevenueOperationStatus.PENDING) { "Only pending creator revenue can complete" }

        status = CreatorRevenueOperationStatus.CONFIRMED
        walletTransactionId = transactionId
        failureReason = null
        nextRetryAt = null
    }

    fun fail(
        reason: String,
        retryAt: Instant,
    ) {
        require(status == CreatorRevenueOperationStatus.PENDING) { "Only pending creator revenue can fail" }
        require(reason.isNotBlank()) { "Creator revenue failure reason cannot be blank" }

        status = CreatorRevenueOperationStatus.FAILED
        failureReason = reason
        nextRetryAt = retryAt
    }

    fun prepareRetry(now: Instant) {
        require(status == CreatorRevenueOperationStatus.FAILED) { "Only failed creator revenue can retry" }

        val retryAt = requireNotNull(nextRetryAt) { "Failed creator revenue must have a retry time" }

        require(!retryAt.isAfter(now)) { "Creator revenue retry is not due yet" }

        requestId = UuidCreator.getTimeOrderedEpoch()
        status = CreatorRevenueOperationStatus.PENDING
        failureReason = null
        nextRetryAt = null
    }

    companion object {
        fun create(
            purchaseOperationId: PurchaseOperationId,
            creatorUserId: UserId,
            amountPrime: Long,
            now: Instant,
        ): CreatorRevenueOperation =
            CreatorRevenueOperation(
                id = CreatorRevenueOperationId.generate(),
                requestId = UuidCreator.getTimeOrderedEpoch(),
                purchaseOperationId = purchaseOperationId,
                creatorUserId = creatorUserId,
                amountPrime = amountPrime,
                createdAt = now,
                status = CreatorRevenueOperationStatus.PENDING,
                walletTransactionId = null,
                failureReason = null,
                nextRetryAt = null,
            )
    }
}
