package com.mnemos.notification.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "notification-account-service-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [NotificationAccountServiceFeignConfiguration::class],
)
fun interface NotificationAccountServiceClient {
    @PostMapping("/internal/v1/users/telegram/{telegramId}/identity/bootstrap")
    fun bootstrapTelegramUserIdentity(
        @PathVariable telegramId: Long,
    ): InternalTelegramUserIdentityResponse
}
