package com.mnemos.wallet.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import com.mnemos.wallet.adapter.out.account.dto.CheckAccountPermissionRequest
import com.mnemos.wallet.adapter.out.account.dto.CheckAccountPermissionResponse
import com.mnemos.wallet.configuration.AccountPermissionFeignConfiguration
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "wallet-account-permission-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [AccountPermissionFeignConfiguration::class],
    dismiss404 = true,
)
interface AccountPermissionClient {
    @GetMapping("/internal/v1/accounts/resolve")
    fun resolve(
        @RequestParam userId: UUID,
    ): InternalAccountResponse?

    @PostMapping("/internal/v1/accounts/{accountId}/permissions/check")
    fun checkPermission(
        @PathVariable accountId: UUID,
        @RequestBody request: CheckAccountPermissionRequest,
    ): CheckAccountPermissionResponse
}
