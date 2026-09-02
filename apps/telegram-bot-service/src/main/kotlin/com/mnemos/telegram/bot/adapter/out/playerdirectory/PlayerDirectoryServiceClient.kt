package com.mnemos.telegram.bot.adapter.out.playerdirectory

import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmSnapshotResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    name = "telegram-bot-player-directory-service-client",
    url = "\${mnemos.clients.player-directory-service.url}",
    configuration = [PlayerDirectoryServiceFeignConfiguration::class],
)
interface PlayerDirectoryServiceClient {
    @GetMapping("/internal/v1/realms/sao")
    fun getSaoSnapshot(): InternalSaoRealmSnapshotResponse
}
