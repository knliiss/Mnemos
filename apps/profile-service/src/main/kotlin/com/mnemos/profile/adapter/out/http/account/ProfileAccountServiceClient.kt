package com.mnemos.profile.adapter.out.http.account

import com.mnemos.contracts.account.internal.v1.InternalAccountBootstrapResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "profile-account-service-client",
    url = "\${mnemos.profile.account-service.base-url}",
    configuration = [ProfileAccountFeignConfiguration::class],
)
interface ProfileAccountServiceClient {
    @GetMapping("/internal/v1/accounts/bootstrap")
    fun fetchBootstrap(
        @RequestParam("afterAccountId", required = false) afterAccountId: UUID?,
        @RequestParam("limit") limit: Int,
    ): List<InternalAccountBootstrapResponse>?
}
