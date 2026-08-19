package com.mnemos.personalization.application.purchase.usecase.get

import com.mnemos.personalization.application.purchase.exception.PurchaseOperationNotFoundException
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPurchaseOperationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: PurchaseOperationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(operationId: PurchaseOperationId): PurchaseOperation {
        val actor = actorProvider.requireActor()
        val operation = repository.findById(operationId)

        if (operation == null || operation.buyerUserId != actor.userId) {
            throw PurchaseOperationNotFoundException(operationId)
        }

        return operation
    }
}
