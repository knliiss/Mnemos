package com.mnemos.playerdirectory.adapter.out.http.cristalix

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "cristalix-player-directory",
    url = "\${mnemos.player-directory.cristalix-api.url}",
    configuration = [CristalixFeignConfiguration::class],
)
interface CristalixPlayerClient {
    @GetMapping("/v1/api/players/{playerId}")
    fun findById(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestHeader("X-Project-Key") projectKey: String,
        @PathVariable playerId: UUID,
        @RequestParam fields: String,
    ): CristalixPlayerResponse

    @GetMapping("/v1/api/players/by-name/{nickname}")
    fun findByNickname(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestHeader("X-Project-Key") projectKey: String,
        @PathVariable nickname: String,
        @RequestParam fields: String,
    ): CristalixPlayerResponse
}
