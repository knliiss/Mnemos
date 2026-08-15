package com.mnemos.personalization.domain.revenue.model

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import java.time.Instant
import java.util.UUID

class CreatorRevenueOperation(
    val id: CreatorRevenueOperationId,
    val requestId: UUID,
    val purchaseOperationId: PurchaseOperationId,
    val creatorUserId: UserId,
    val amountPrime: Long,
    val createdAt: Instant,
    status: CreatorRevenueOperationStatus,
    walletTransactionId: UUID?,
    failureReason: String?,
) {
    var status: CreatorRevenueOperationStatus = status
        private set
    var walletTransactionId: UUID? = walletTransactionId
        private set
    var failureReason: String? = failureReason
        private set

    init {
        require(amountPrime > 0) { "Creator revenue must be positive" }
    }

    fun complete(transactionId: UUID) {
        require(status == CreatorRevenueOperationStatus.PENDING) { "Only pending creator revenue can complete" }

        status = CreatorRevenueOperationStatus.CONFIRMED
        walletTransactionId = transactionId
        failureReason = null
    }

    fun fail(reason: String) {
        require(status == CreatorRevenueOperationStatus.PENDING) { "Only pending creator revenue can fail" }
        require(reason.isNotBlank()) { "Creator revenue failure reason cannot be blank" }

        status = CreatorRevenueOperationStatus.FAILED
        failureReason = reason
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
            )
    }
}
