package com.mnemos.payment.adapter.inbound.http.internal

import com.mnemos.payment.application.stars.RecordSuccessfulStarPaymentService
import com.mnemos.payment.application.stars.ValidateStarPreCheckoutService
import com.mnemos.payment.contracts.internal.telegram.RecordSuccessfulStarPaymentRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/stars")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramPaymentInternalController(
    private val validatePreCheckoutService: ValidateStarPreCheckoutService,
    private val recordSuccessfulPaymentService: RecordSuccessfulStarPaymentService,
) {
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
}
