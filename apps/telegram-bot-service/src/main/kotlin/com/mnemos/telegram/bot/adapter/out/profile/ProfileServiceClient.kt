package com.mnemos.telegram.bot.adapter.out.profile

import com.mnemos.contracts.profile.internal.v1.InternalTelegramProfileSearchPage
import com.mnemos.contracts.profile.internal.v1.InternalTelegramPublicProfileSummary
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "telegram-bot-profile-service-client",
    url = "\${mnemos.clients.profile-service.url}",
    configuration = [ProfileServiceFeignConfiguration::class],
    dismiss404 = true,
)
interface ProfileServiceClient {
    @GetMapping("/internal/v1/telegram/profiles")
    fun search(
        @RequestParam("q") query: String,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): InternalTelegramProfileSearchPage

    @GetMapping("/internal/v1/telegram/profiles/{accountId}")
    fun get(@PathVariable accountId: UUID): InternalTelegramPublicProfileSummary?
}
