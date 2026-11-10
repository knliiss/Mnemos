package com.mnemos.social.adapter.out.http.account

import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserContextResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "social-account-service-client",
    url = "\${mnemos.social.account-service.base-url}",
    configuration = [SocialAccountFeignConfiguration::class],
    dismiss404 = true,
)
interface AccountServiceClient {
    @GetMapping("/internal/v1/users/telegram/{telegramId}/context")
    fun getTelegramUserContext(@PathVariable telegramId: Long): InternalTelegramUserContextResponse?

    @GetMapping("/internal/v1/accounts/resolve")
    fun resolveActiveAccount(
        @RequestParam userId: UUID,
    ): InternalAccountResponse?

    @GetMapping("/internal/v1/accounts/resolve")
    fun findActiveAccount(
        @RequestParam accountId: UUID,
    ): InternalAccountResponse?

    @PostMapping("/internal/v1/accounts/{accountId}/permissions/check")
    fun checkPermission(
        @PathVariable accountId: UUID,
        @RequestBody request: CheckAccountPermissionRequest,
    ): CheckAccountPermissionResponse
}
