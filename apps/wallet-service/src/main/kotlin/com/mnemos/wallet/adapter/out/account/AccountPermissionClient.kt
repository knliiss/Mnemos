package com.mnemos.wallet.adapter.out.account

import com.mnemos.wallet.adapter.out.account.dto.CheckAccountPermissionRequest
import com.mnemos.wallet.adapter.out.account.dto.CheckAccountPermissionResponse
import com.mnemos.wallet.configuration.AccountPermissionFeignConfiguration
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@FeignClient(
    name = "wallet-account-permission-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [AccountPermissionFeignConfiguration::class],
)
interface AccountPermissionClient {
    @PostMapping("/internal/v1/accounts/{accountId}/permissions/check")
    fun checkPermission(
        @PathVariable accountId: UUID,
        @RequestBody request: CheckAccountPermissionRequest,
    ): CheckAccountPermissionResponse
}
