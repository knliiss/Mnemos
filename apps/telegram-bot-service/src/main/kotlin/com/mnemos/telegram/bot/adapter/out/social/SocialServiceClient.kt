package com.mnemos.telegram.bot.adapter.out.social

import com.mnemos.social.contracts.internal.v1.InternalTelegramCommentPage
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "telegram-bot-social-service-client",
    url = "\${mnemos.clients.social-service.url}",
    configuration = [SocialServiceFeignConfiguration::class],
)
interface SocialServiceClient {
    @GetMapping("/internal/v1/telegram/social/profiles/{accountId}/comments")
    fun listComments(
        @PathVariable accountId: UUID,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): InternalTelegramCommentPage
}
