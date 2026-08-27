package com.mnemos.account.integration.playerdirectory

import com.mnemos.contracts.playerdirectory.internal.v1.InternalPlayerResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalResolvePlayerRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "account-player-directory-client",
    url = "\${mnemos.account.player-directory-service.base-url}",
    configuration = [PlayerDirectoryFeignConfiguration::class],
)
interface PlayerDirectoryClient {
    @PostMapping("/internal/v1/players/resolve")
    fun resolve(
        @RequestBody request: InternalResolvePlayerRequest,
    ): InternalPlayerResponse
}
