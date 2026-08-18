package com.mnemos.personalization.domain.purchase.repository

import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import java.util.UUID

interface PurchaseOperationRepository {
    fun save(operation: PurchaseOperation): PurchaseOperation

    fun findById(id: PurchaseOperationId): PurchaseOperation?

    fun findByIdempotencyKey(idempotencyKey: UUID): PurchaseOperation?

    fun findByIdForUpdate(id: PurchaseOperationId): PurchaseOperation?

    fun findByRequestIdForUpdate(requestId: UUID): PurchaseOperation?

    fun existsPending(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): Boolean
}
