package com.mnemos.personalization.adapter.out.persistence.purchase

import com.mnemos.personalization.adapter.out.persistence.purchase.PurchaseOperationPersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.purchase.PurchaseOperationPersistenceMapper.toEntity
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PurchaseOperationRepositoryAdapter(
    private val repository: PurchaseOperationJpaRepository,
) : PurchaseOperationRepository {
    override fun save(operation: PurchaseOperation): PurchaseOperation = repository.saveAndFlush(operation.toEntity()).toDomain()

    override fun findById(id: PurchaseOperationId): PurchaseOperation? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdForUpdate(id: PurchaseOperationId): PurchaseOperation? = repository.findByIdForUpdate(id.id)?.toDomain()

    override fun findByRequestIdForUpdate(requestId: UUID): PurchaseOperation? = repository.findByRequestId(requestId)?.toDomain()

    override fun existsPending(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): Boolean =
        repository.existsByOwnerTypeAndOwnerIdAndPersonalizationIdAndStatus(
            ownerType = owner.target,
            ownerId = owner.id,
            personalizationId = personalizationId.id,
            status = PurchaseOperationStatus.PENDING,
        )
}
