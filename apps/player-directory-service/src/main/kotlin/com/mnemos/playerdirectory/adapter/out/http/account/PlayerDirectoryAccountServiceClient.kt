package com.mnemos.playerdirectory.adapter.out.http.account

import com.mnemos.contracts.account.internal.v1.InternalAccountBootstrapResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "player-directory-account-service-client",
    url = "\${mnemos.player-directory.account-service.base-url}",
    configuration = [PlayerDirectoryAccountFeignConfiguration::class],
)
interface PlayerDirectoryAccountServiceClient {
    @GetMapping("/internal/v1/accounts/bootstrap")
    fun fetchBootstrap(
        @RequestParam("afterAccountId", required = false) afterAccountId: UUID?,
        @RequestParam("limit") limit: Int,
    ): List<InternalAccountBootstrapResponse>?
}
