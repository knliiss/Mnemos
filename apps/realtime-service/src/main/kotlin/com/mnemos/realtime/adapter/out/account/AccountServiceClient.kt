package com.mnemos.realtime.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalAccountMemberUserIdsResponse
import com.mnemos.realtime.configuration.account.AccountServiceFeignConfiguration
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@FeignClient(
    name = "realtime-account-service-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [AccountServiceFeignConfiguration::class],
    dismiss404 = true,
)
interface AccountServiceClient {
    @GetMapping("/internal/v1/accounts/{accountId}/members/user-ids")
    fun findMemberUserIds(
        @PathVariable accountId: UUID,
    ): InternalAccountMemberUserIdsResponse?
}
