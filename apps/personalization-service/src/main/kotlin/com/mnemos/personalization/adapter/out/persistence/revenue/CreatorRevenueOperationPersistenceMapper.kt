package com.mnemos.personalization.adapter.out.persistence.revenue

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationId

object CreatorRevenueOperationPersistenceMapper {
    fun CreatorRevenueOperation.toEntity(): CreatorRevenueOperationEntity =
        CreatorRevenueOperationEntity(
            id = id.id,
            requestId = requestId,
            purchaseOperationId = purchaseOperationId.id,
            creatorUserId = creatorUserId.id,
            amountPrime = amountPrime,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            nextRetryAt = nextRetryAt,
            createdAt = createdAt,
        )

    fun CreatorRevenueOperationEntity.toDomain(): CreatorRevenueOperation =
        CreatorRevenueOperation(
            id = CreatorRevenueOperationId(id),
            requestId = requestId,
            purchaseOperationId = PurchaseOperationId(purchaseOperationId),
            creatorUserId = UserId(creatorUserId),
            amountPrime = amountPrime,
            createdAt = createdAt,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            nextRetryAt = nextRetryAt,
        )
}
