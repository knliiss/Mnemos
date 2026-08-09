package com.mnemos.wallet.integration.account.client

import com.mnemos.wallet.integration.account.config.AccountPermissionFeignConfiguration
import com.mnemos.wallet.integration.account.dto.CheckAccountPermissionRequest
import com.mnemos.wallet.integration.account.dto.CheckAccountPermissionResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@FeignClient(
    name = "accountPermissionClient",
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
