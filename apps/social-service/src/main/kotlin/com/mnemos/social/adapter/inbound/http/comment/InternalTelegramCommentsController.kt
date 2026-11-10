package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.comment.usecase.get.GetCommentUseCase
import com.mnemos.social.application.comment.usecase.list.ListCommentRepliesQuery
import com.mnemos.social.application.comment.usecase.list.ListCommentsUseCase
import com.mnemos.social.application.comment.usecase.list.ListRootCommentsQuery
import com.mnemos.social.application.common.pagination.PageQuery
import com.mnemos.social.application.common.pagination.PageResult
import com.mnemos.social.contracts.internal.v1.InternalTelegramComment
import com.mnemos.social.contracts.internal.v1.InternalTelegramCommentPage
import com.mnemos.social.domain.comment.model.CommentId
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@Validated
@RequestMapping("/internal/v1/telegram/social")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramCommentsController(
    private val comments: ListCommentsUseCase,
    private val getComment: GetCommentUseCase,
    private val actors: ResolveTelegramSocialActorUseCase,
    private val access: AccountAccessPort,
) {
    @GetMapping("/profiles/{accountId}/comments")
    fun list(
        @PathVariable accountId: UUID,
        @RequestParam @Min(0) @Max(1_000) page: Int,
        @RequestParam @Min(1) @Max(8) size: Int,
        @RequestParam telegramId: Long = 0,
    ): InternalTelegramCommentPage {
        val result = comments.listRoot(ListRootCommentsQuery(accountId, PageQuery(page, size)))
        return result.toInternalPage(resolveActor(telegramId), accountId)
    }

    @GetMapping("/profiles/{accountId}/comments/{rootId}/replies")
    fun replies(
        @PathVariable accountId: UUID,
        @PathVariable rootId: UUID,
        @RequestParam @Min(0) @Max(1_000) page: Int,
        @RequestParam @Min(1) @Max(8) size: Int,
        @RequestParam telegramId: Long = 0,
    ): InternalTelegramCommentPage {
        val root = getComment.execute(rootId)
        if (root.targetAccountId != accountId || root.parentCommentId != null || root.deleted) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
        return comments.listReplies(ListCommentRepliesQuery(CommentId(rootId), PageQuery(page, size)))
            .toInternalPage(resolveActor(telegramId), accountId)
    }

    private fun resolveActor(telegramId: Long): SocialActor? =
        if (telegramId <= 0) null else runCatching { actors.resolve(telegramId) }.getOrNull()

    private fun PageResult<CommentView>.toInternalPage(
        actor: SocialActor?,
        accountId: UUID,
    ): InternalTelegramCommentPage {
        val canModerate = actor != null && runCatching {
            access.hasPermission(accountId, actor.userId, RolePermission.MODERATE_COMMENTS)
        }.getOrDefault(false)
        return InternalTelegramCommentPage(
            items = items.map { comment ->
                InternalTelegramComment(
                    id = comment.id.id,
                    authorAccountId = comment.authorAccountId,
                    content = if (comment.deleted) null else comment.content,
                    deleted = comment.deleted,
                    likes = comment.likes,
                    replies = comment.replies,
                    createdAt = comment.createdAt,
                    canEdit = !comment.deleted && actor?.userId == comment.authorUserId,
                    canDelete = !comment.deleted && (
                        actor?.userId == comment.authorUserId || canModerate
                    ),
                )
            },
            page = page,
            totalPages = totalPages,
            totalItems = totalItems,
        )
    }
}
