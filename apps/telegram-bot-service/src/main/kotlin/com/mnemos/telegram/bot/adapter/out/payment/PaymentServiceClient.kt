package com.mnemos.telegram.bot.adapter.out.payment

import com.mnemos.payment.contracts.internal.telegram.RecordSuccessfulStarPaymentRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutRequest
import com.mnemos.payment.contracts.internal.telegram.ValidateStarPreCheckoutResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "telegram-bot-payment-service-client",
    url = "\${mnemos.clients.payment-service.url}",
    configuration = [PaymentServiceFeignConfiguration::class],
)
interface PaymentServiceClient {
    @PostMapping("/internal/v1/telegram/stars/pre-checkout")
    fun validateStarPreCheckout(
        @RequestBody request: ValidateStarPreCheckoutRequest,
    ): ValidateStarPreCheckoutResponse

    @PostMapping("/internal/v1/telegram/stars/successful-payments")
    fun recordSuccessfulStarPayment(
        @RequestBody request: RecordSuccessfulStarPaymentRequest,
    )
}
