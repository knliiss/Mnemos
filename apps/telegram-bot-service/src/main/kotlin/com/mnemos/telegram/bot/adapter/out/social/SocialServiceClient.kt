package com.mnemos.telegram.bot.adapter.out.social

import com.mnemos.social.contracts.internal.v1.InternalTelegramCommentPage
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionState
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
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
    @GetMapping("/internal/v1/telegram/social/users/{telegramId}/profiles/{accountId}/reaction")
    fun getReaction(@PathVariable telegramId: Long, @PathVariable accountId: UUID): InternalTelegramReactionState

    @PutMapping("/internal/v1/telegram/social/users/{telegramId}/profiles/{accountId}/reaction")
    fun setReaction(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
        @RequestBody request: InternalTelegramReactionRequest,
    )

    @DeleteMapping("/internal/v1/telegram/social/users/{telegramId}/profiles/{accountId}/reaction")
    fun removeReaction(@PathVariable telegramId: Long, @PathVariable accountId: UUID)

    @GetMapping("/internal/v1/telegram/social/profiles/{accountId}/comments")
    fun listComments(
        @PathVariable accountId: UUID,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): InternalTelegramCommentPage

    @GetMapping("/internal/v1/telegram/social/profiles/{accountId}/comments/{rootId}/replies")
    fun listReplies(
        @PathVariable accountId: UUID,
        @PathVariable rootId: UUID,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): InternalTelegramCommentPage
}
