package com.mnemos.payment.adapter.inbound.http.stars

import com.mnemos.payment.application.stars.CreateStarPrimePurchaseService
import com.mnemos.payment.application.stars.GetStarPrimePurchaseService
import com.mnemos.payment.application.stars.StarPrimePackageCatalog
import com.mnemos.payment.application.stars.model.CreatedStarPrimePurchase
import com.mnemos.payment.application.stars.model.StarPrimePackage
import com.mnemos.payment.application.stars.model.StarPrimePurchaseView
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/payments/stars/prime")
class StarPrimePurchaseController(
    private val packageCatalog: StarPrimePackageCatalog,
    private val createPurchaseService: CreateStarPrimePurchaseService,
    private val getPurchaseService: GetStarPrimePurchaseService,
) {
    @GetMapping("/catalog")
    fun catalog(): List<StarPrimePackageResponse> =
        packageCatalog.all().map(StarPrimePackage::toResponse)

    @PostMapping("/purchases")
    fun create(
        @Valid @RequestBody request: CreateStarPrimePurchaseRequest,
    ): CreatedStarPrimePurchaseResponse =
        createPurchaseService.execute(request.packageId).toResponse()

    @GetMapping("/purchases/{purchaseId}")
    fun get(
        @PathVariable purchaseId: UUID,
    ): StarPrimePurchaseResponse =
        getPurchaseService.execute(purchaseId).toResponse()

    private fun StarPrimePackage.toResponse(): StarPrimePackageResponse =
        StarPrimePackageResponse(
            id = id,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
        )

    private fun CreatedStarPrimePurchase.toResponse(): CreatedStarPrimePurchaseResponse =
        CreatedStarPrimePurchaseResponse(
            purchaseId = purchaseId,
            packageId = packageId,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
            status = status,
            invoiceUrl = invoiceUrl,
        )

    private fun StarPrimePurchaseView.toResponse(): StarPrimePurchaseResponse =
        StarPrimePurchaseResponse(
            purchaseId = purchaseId,
            packageId = packageId,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
            status = status,
        )
}
