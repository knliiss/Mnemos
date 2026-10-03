package com.mnemos.notification.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalActiveAccountUsersResponse
import com.mnemos.contracts.account.internal.v1.InternalActiveAccountUsersResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import com.mnemos.contracts.account.internal.v1.InternalUserLocalizationResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "notification-account-service-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [NotificationAccountServiceFeignConfiguration::class],
)
interface NotificationAccountServiceClient {
    @PostMapping("/internal/v1/users/telegram/{telegramId}/identity/bootstrap")
    fun bootstrapTelegramUserIdentity(
        @PathVariable telegramId: Long,
    ): InternalTelegramUserIdentityResponse

    @GetMapping("/internal/v1/notification/active-account-users")
    fun findActiveAccountUsers(
        @RequestParam nickname: String,
    ): InternalActiveAccountUsersResponse

    @GetMapping("/internal/v1/notification/active-account-users")
    fun findActiveAccountUsers(
        @RequestParam nickname: String,
    ): InternalActiveAccountUsersResponse

    @GetMapping("/internal/v1/users/{userId}/localization")
    fun getUserLocalization(
        @PathVariable userId: UUID,
    ): InternalUserLocalizationResponse
}
