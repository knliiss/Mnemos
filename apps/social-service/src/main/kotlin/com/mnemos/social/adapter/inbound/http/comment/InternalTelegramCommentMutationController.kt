package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentCommand
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentUseCase
import com.mnemos.social.application.comment.telegram.TelegramCommentCreationResult
import com.mnemos.social.contracts.internal.v1.InternalTelegramCreateCommentRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramCreateCommentResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/telegram/social/users/{telegramId}/profiles/{accountId}/comments")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramCommentMutationController(
    private val actors: ResolveTelegramSocialActorUseCase,
    private val createComment: CreateTelegramCommentUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
        @RequestBody request: InternalTelegramCreateCommentRequest,
    ): InternalTelegramCreateCommentResponse {
        if (request.content.isBlank() || request.content.trim().length > 500) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must contain 1..500 characters")
        }
        val actor = actors.resolve(telegramId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Active Telegram account required")
        if (actor.accountId != request.expectedActorAccountId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Active account changed")
        }
        return when (
            val result = createComment.execute(
                CreateTelegramCommentCommand(
                    telegramId = telegramId,
                    actor = actor,
                    operationId = request.operationId,
                    targetAccountId = accountId,
                    parentCommentId = request.parentCommentId,
                    content = request.content,
                ),
            )
        ) {
            is TelegramCommentCreationResult.Created -> InternalTelegramCreateCommentResponse(result.commentId)
            TelegramCommentCreationResult.Conflict ->
                throw ResponseStatusException(HttpStatus.CONFLICT, "Telegram comment operation conflicts")
        }
    }
}
