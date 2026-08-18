package com.mnemos.personalization.domain.revenue.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import java.util.UUID

interface CreatorRevenueOperationRepository {
    fun save(operation: CreatorRevenueOperation): CreatorRevenueOperation

    fun findByRequestIdForUpdate(requestId: UUID): CreatorRevenueOperation?

    fun findByCreatorUserId(creatorUserId: UserId): List<CreatorRevenueOperation>

    fun existsByPurchaseOperationId(purchaseOperationId: PurchaseOperationId): Boolean
}
