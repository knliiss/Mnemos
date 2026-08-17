package com.mnemos.personalization.adapter.out.persistence.revenue

import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface CreatorRevenueOperationJpaRepository : JpaRepository<CreatorRevenueOperationEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByRequestId(requestId: UUID): CreatorRevenueOperationEntity?

    fun findAllByCreatorUserIdOrderByCreatedAtDesc(creatorUserId: UUID): List<CreatorRevenueOperationEntity>

    fun existsByPurchaseOperationId(purchaseOperationId: UUID): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT operation
        FROM CreatorRevenueOperationEntity operation
        WHERE operation.status = :status
          AND operation.nextRetryAt <= :now
        ORDER BY operation.nextRetryAt ASC, operation.id ASC
        """,
    )
    fun findDueForRetry(
        status: CreatorRevenueOperationStatus,
        now: Instant,
        pageable: Pageable,
    ): List<CreatorRevenueOperationEntity>
}
