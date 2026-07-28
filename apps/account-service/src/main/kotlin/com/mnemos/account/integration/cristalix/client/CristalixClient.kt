package com.mnemos.account.integration.cristalix.client

import com.mnemos.account.integration.cristalix.config.CristalixFeignConfig
import com.mnemos.account.integration.cristalix.dto.CristalixPlayerResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "cristalix",
    url = "\${mnemos.cristalix-api.url}",
    configuration = [CristalixFeignConfig::class],
)
interface CristalixClient {
    @GetMapping("/v1/api/players/by-name/{nickname}")
    fun findPlayer(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestHeader("X-Project-Key") projectKey: String,
        @PathVariable nickname: String,
        @RequestParam fields: String = "playerId,name",
    ): CristalixPlayerResponse
}
