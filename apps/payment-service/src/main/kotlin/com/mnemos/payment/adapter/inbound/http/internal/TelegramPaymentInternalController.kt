package com.mnemos.payment.adapter.inbound.http.internal

import com.mnemos.payment.application.stars.CreateTelegramStarPrimePurchaseService
import com.mnemos.payment.application.stars.RecordSuccessfulStarPaymentService
import com.mnemos.payment.application.stars.StarPrimePackageCatalog
import com.mnemos.payment.application.stars.ValidateStarPreCheckoutService
import com.mnemos.payment.application.stars.model.CreatedStarPrimePurchase
import com.mnemos.payment.application.stars.model.StarPrimePackage
import com.mnemos.payment.contracts.internal.telegram.CreateTelegramStarPrimePurchaseRequest
import com.mnemos.payment.contracts.internal.telegram.CreatedTelegramStarPrimePurchaseResponse
import com.mnemos.payment.contracts.internal.telegram.RecordSuccessfulStarPaymentRequest
import com.mnemos.payment.contracts.internal.telegram.TelegramStarPrimePackageResponse
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/stars")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramPaymentInternalController(
    private val packageCatalog: StarPrimePackageCatalog,
    private val createPurchaseService: CreateTelegramStarPrimePurchaseService,
    private val validatePreCheckoutService: ValidateStarPreCheckoutService,
    private val recordSuccessfulPaymentService: RecordSuccessfulStarPaymentService,
) {
    @GetMapping("/prime/catalog")
    fun catalog(): List<TelegramStarPrimePackageResponse> = packageCatalog.all().map { primePackage -> primePackage.toTelegramResponse() }

    @PostMapping("/prime/purchases")
    fun createPurchase(
        @Valid @RequestBody request: CreateTelegramStarPrimePurchaseRequest,
    ): CreatedTelegramStarPrimePurchaseResponse =
        createPurchaseService
            .execute(
                telegramId = request.telegramId,
                packageId = request.packageId,
            ).toTelegramResponse()

    @PostMapping("/pre-checkout")
    fun validatePreCheckout(
        @Valid @RequestBody request: ValidateStarPreCheckoutRequest,
    ): ValidateStarPreCheckoutResponse {
        val decision =
            validatePreCheckoutService.execute(
                telegramId = request.telegramId,
                invoicePayload = request.invoicePayload,
                currency = request.currency,
                totalAmount = request.totalAmount,
            )

        return ValidateStarPreCheckoutResponse(
            approved = decision.approved,
            errorMessage = decision.errorMessage,
        )
    }

    @PostMapping("/successful-payments")
    fun recordSuccessfulPayment(
        @Valid @RequestBody request: RecordSuccessfulStarPaymentRequest,
    ) {
        recordSuccessfulPaymentService.execute(
            telegramId = request.telegramId,
            invoicePayload = request.invoicePayload,
            currency = request.currency,
            totalAmount = request.totalAmount,
            telegramPaymentChargeId = request.telegramPaymentChargeId,
        )
    }

    private fun StarPrimePackage.toTelegramResponse(): TelegramStarPrimePackageResponse =
        TelegramStarPrimePackageResponse(
            id = id,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
        )

    private fun CreatedStarPrimePurchase.toTelegramResponse(): CreatedTelegramStarPrimePurchaseResponse =
        CreatedTelegramStarPrimePurchaseResponse(
            purchaseId = purchaseId,
            packageId = packageId,
            primeAmount = primeAmount,
            starsAmount = starsAmount,
            invoiceUrl = invoiceUrl,
        )
}
