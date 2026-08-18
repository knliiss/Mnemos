package com.mnemos.personalization.adapter.out.persistence.revenue

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.revenue.CreatorRevenueOperationPersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.revenue.CreatorRevenueOperationPersistenceMapper.toEntity
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueRetryRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class CreatorRevenueOperationRepositoryAdapter(
    private val repository: CreatorRevenueOperationJpaRepository,
) : CreatorRevenueOperationRepository,
    CreatorRevenueRetryRepository {
    override fun save(operation: CreatorRevenueOperation): CreatorRevenueOperation =
        repository.saveAndFlush(operation.toEntity()).toDomain()

    override fun findByRequestIdForUpdate(requestId: UUID): CreatorRevenueOperation? = repository.findByRequestId(requestId)?.toDomain()

    override fun findByCreatorUserId(creatorUserId: UserId): List<CreatorRevenueOperation> =
        repository
            .findAllByCreatorUserIdOrderByCreatedAtDesc(creatorUserId.id)
            .map { entity -> entity.toDomain() }

    override fun existsByPurchaseOperationId(purchaseOperationId: PurchaseOperationId): Boolean =
        repository.existsByPurchaseOperationId(purchaseOperationId.id)

    override fun findDueForRetryForUpdate(
        now: Instant,
        limit: Int,
    ): List<CreatorRevenueOperation> =
        repository
            .findDueForRetry(
                status = CreatorRevenueOperationStatus.FAILED,
                now = now,
                pageable = PageRequest.of(0, limit),
            ).map { it.toDomain() }
}
