package com.mnemos.personalization.adapter.out.account

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "personalizationAccountInternalClient",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [PersonalizationAccountInternalFeignConfiguration::class],
    dismiss404 = true,
)
interface PersonalizationAccountInternalClient {
    @GetMapping("/internal/v1/users/telegram/{telegramId}/identity")
    fun getTelegramUserIdentity(
        @PathVariable telegramId: Long,
    ): InternalTelegramUserIdentityResponse?

    @GetMapping("/internal/v1/accounts/resolve")
    fun resolve(
        @RequestParam(required = false) userId: UUID? = null,
    ): InternalAccountResponse?

    @PostMapping("/internal/v1/accounts/{accountId}/permissions/check")
    fun checkPermission(
        @PathVariable accountId: UUID,
        @RequestBody request: CheckAccountPermissionRequest,
    ): CheckAccountPermissionResponse
}

data class CheckAccountPermissionRequest(
    val userId: UUID,
    val permission: RolePermission,
)

data class CheckAccountPermissionResponse(
    val granted: Boolean,
)
