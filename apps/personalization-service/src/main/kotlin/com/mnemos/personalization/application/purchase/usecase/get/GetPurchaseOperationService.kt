package com.mnemos.personalization.application.purchase.usecase.get

import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class GetPurchaseOperationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: PurchaseOperationRepository,
) {
    @Transactional(readOnly = true)
    fun execute(operationId: PurchaseOperationId): PurchaseOperation {
        val actor = actorProvider.requireActor()
        val operation =
            repository.findById(operationId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase operation was not found")

        if (operation.buyerUserId != actor.userId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase operation was not found")
        }

        return operation
    }
}
