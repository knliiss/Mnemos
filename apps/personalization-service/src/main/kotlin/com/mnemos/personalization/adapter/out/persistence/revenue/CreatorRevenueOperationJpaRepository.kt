package com.mnemos.personalization.adapter.out.persistence.revenue

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.UUID

interface CreatorRevenueOperationJpaRepository : JpaRepository<CreatorRevenueOperationEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByRequestId(requestId: UUID): CreatorRevenueOperationEntity?

    fun existsByPurchaseOperationId(purchaseOperationId: UUID): Boolean
}
