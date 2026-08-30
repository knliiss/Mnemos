package com.mnemos.payment.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramUserContextResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "payment-account-service-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [PaymentAccountServiceFeignConfiguration::class],
    dismiss404 = true,
)
fun interface PaymentAccountServiceClient {
    @GetMapping("/internal/v1/users/telegram/{telegramId}/context")
    fun getTelegramUserContext(
        @PathVariable telegramId: Long,
    ): InternalTelegramUserContextResponse?
}
