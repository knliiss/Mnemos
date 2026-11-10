package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.comment.usecase.get.GetCommentUseCase
import com.mnemos.social.application.comment.usecase.edit.EditCommentCommand
import com.mnemos.social.application.comment.usecase.edit.EditCommentForActorUseCase
import com.mnemos.social.application.comment.usecase.delete.DeleteCommentForActorUseCase
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentCommand
import com.mnemos.social.application.comment.telegram.CreateTelegramCommentUseCase
import com.mnemos.social.application.comment.telegram.TelegramCommentCreationResult
import com.mnemos.social.contracts.internal.v1.InternalTelegramCreateCommentRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramCreateCommentResponse
import com.mnemos.social.contracts.internal.v1.InternalTelegramEditCommentRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramDeleteCommentRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.DeleteMapping
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
    private val editComment: EditCommentForActorUseCase,
    private val deleteComment: DeleteCommentForActorUseCase,
    private val getComment: GetCommentUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
        @RequestBody request: InternalTelegramCreateCommentRequest,
    ): InternalTelegramCreateCommentResponse {
        validateBody(request.content)
        val actor = requireActor(telegramId, request.expectedActorAccountId)
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

    @PatchMapping("/{commentId}")
    fun edit(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
        @PathVariable commentId: UUID,
        @RequestBody request: InternalTelegramEditCommentRequest,
    ): InternalTelegramCreateCommentResponse {
        validateBody(request.content)
        val actor = requireActor(telegramId, request.expectedActorAccountId)
        requireCommentFromAccount(commentId, accountId)
        val updated = editComment.executeForActor(EditCommentCommand(CommentId(commentId), request.content.trim()), actor)
        return InternalTelegramCreateCommentResponse(updated.id.id)
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
        @PathVariable commentId: UUID,
        @RequestBody request: InternalTelegramDeleteCommentRequest,
    ) {
        val actor = requireActor(telegramId, request.expectedActorAccountId)
        requireCommentFromAccount(commentId, accountId)
        deleteComment.executeForActor(commentId, actor)
    }

    private fun requireActor(telegramId: Long, expectedAccountId: UUID): SocialActor {
        val actor = actors.resolve(telegramId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Active Telegram account required")
        if (actor.accountId != expectedAccountId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Active account changed")
        }
        return actor
    }

    private fun requireCommentFromAccount(commentId: UUID, accountId: UUID) {
        val comment = getComment.execute(commentId)
        if (comment.targetAccountId != accountId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found in profile")
        }
    }

    private fun validateBody(text: String) {
        if (text.isBlank() || text.trim().length > 500) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must contain 1..500 characters")
        }
    }
}
