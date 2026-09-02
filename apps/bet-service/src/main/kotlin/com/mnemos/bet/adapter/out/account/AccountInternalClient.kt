package com.mnemos.bet.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import com.mnemos.contracts.account.internal.v1.InternalAccountSummariesRequest
import com.mnemos.contracts.account.internal.v1.InternalAccountSummaryResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "betAccountInternalClient",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [AccountInternalFeignConfiguration::class],
    dismiss404 = true,
)
interface AccountInternalClient {
    @GetMapping("/internal/v1/accounts/resolve")
    fun resolve(
        @RequestParam(required = false) userId: UUID? = null,
        @RequestParam(required = false) cristalixNickname: String? = null,
    ): InternalAccountResponse?

    @GetMapping("/internal/v1/users/telegram/{telegramId}/identity")
    fun getTelegramUserIdentity(
        @PathVariable telegramId: Long,
    ): InternalTelegramUserIdentityResponse?

    @PostMapping("/internal/v1/accounts/{accountId}/permissions/check")
    fun checkPermission(
        @PathVariable accountId: UUID,
        @RequestBody request: CheckAccountPermissionRequest,
    ): CheckAccountPermissionResponse

    @PostMapping("/internal/v1/accounts/summaries")
    fun summaries(
        @RequestBody request: InternalAccountSummariesRequest,
    ): List<InternalAccountSummaryResponse>
}
