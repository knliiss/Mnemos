package com.mnemos.personalization.adapter.inbound.http.purchase

import com.mnemos.common.web.logging.MnemosHeaders.IDEMPOTENCY_KEY
import com.mnemos.personalization.adapter.inbound.http.purchase.PurchaseOperationApiMapper.toResponse
import com.mnemos.personalization.application.purchase.usecase.get.GetPurchaseOperationService
import com.mnemos.personalization.application.purchase.usecase.purchase.PurchasePersonalizationUseCase
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations")
class PurchasePersonalizationController(
    private val purchasePersonalizationUseCase: PurchasePersonalizationUseCase,
    private val getPurchaseOperationService: GetPurchaseOperationService,
) {
    @PostMapping("/{personalizationId}/purchase")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun purchase(
        @RequestHeader(IDEMPOTENCY_KEY) idempotencyKey: UUID,
        @PathVariable personalizationId: UUID,
    ): PurchasePersonalizationResponse {
        val result =
            purchasePersonalizationUseCase.execute(
                personalizationId = PersonalizationId(personalizationId),
                idempotencyKey = idempotencyKey,
            )

        return PurchasePersonalizationResponse(
            personalizationId = result.personalizationId.id,
            operationId = result.operationId.id,
        )
    }

    @GetMapping("/purchases/{operationId}")
    fun getPurchaseOperation(
        @PathVariable operationId: UUID,
    ): PurchaseOperationResponse =
        getPurchaseOperationService
            .execute(PurchaseOperationId(operationId))
            .toResponse()
}
