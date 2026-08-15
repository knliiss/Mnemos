package com.mnemos.personalization.adapter.out.persistence.revenue

import com.mnemos.personalization.adapter.out.persistence.revenue.CreatorRevenueOperationPersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.revenue.CreatorRevenueOperationPersistenceMapper.toEntity
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CreatorRevenueOperationRepositoryAdapter(
    private val repository: CreatorRevenueOperationJpaRepository,
) : CreatorRevenueOperationRepository {
    override fun save(operation: CreatorRevenueOperation): CreatorRevenueOperation =
        repository.saveAndFlush(operation.toEntity()).toDomain()

    override fun findByRequestIdForUpdate(requestId: UUID): CreatorRevenueOperation? =
        repository.findByRequestId(requestId)?.toDomain()

    override fun existsByPurchaseOperationId(purchaseOperationId: PurchaseOperationId): Boolean =
        repository.existsByPurchaseOperationId(purchaseOperationId.id)
}
