package com.mnemos.personalization.adapter.inbound.http.purchase

import com.mnemos.personalization.application.purchase.usecase.purchase.PurchasePersonalizationUseCase
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations")
class PurchasePersonalizationController(
    private val purchasePersonalizationUseCase: PurchasePersonalizationUseCase,
) {
    @PostMapping("/{personalizationId}/purchase")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun purchase(
        @PathVariable personalizationId: UUID,
    ): PurchasePersonalizationResponse {
        val result = purchasePersonalizationUseCase.execute(PersonalizationId(personalizationId))

        return PurchasePersonalizationResponse(
            personalizationId = result.personalizationId.id,
            operationId = result.operationId.id,
        )
    }
}
