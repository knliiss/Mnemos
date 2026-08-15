package com.mnemos.personalization.adapter.out.persistence.purchase

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.UUID

interface PurchaseOperationJpaRepository : JpaRepository<PurchaseOperationEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByRequestId(requestId: UUID): PurchaseOperationEntity?

    fun existsByOwnerTypeAndOwnerIdAndPersonalizationIdAndStatus(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationId: UUID,
        status: PurchaseOperationStatus,
    ): Boolean
}
