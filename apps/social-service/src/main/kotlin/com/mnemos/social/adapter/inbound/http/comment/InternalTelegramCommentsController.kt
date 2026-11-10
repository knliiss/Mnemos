package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.comment.usecase.list.ListCommentsUseCase
import com.mnemos.social.application.comment.usecase.list.ListRootCommentsQuery
import com.mnemos.social.application.common.pagination.PageQuery
import com.mnemos.social.contracts.internal.v1.InternalTelegramComment
import com.mnemos.social.contracts.internal.v1.InternalTelegramCommentPage
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Validated
@RequestMapping("/internal/v1/telegram/social")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramCommentsController(
    private val comments: ListCommentsUseCase,
) {
    @GetMapping("/profiles/{accountId}/comments")
    fun list(
        @PathVariable accountId: UUID,
        @RequestParam @Min(0) @Max(1_000) page: Int,
        @RequestParam @Min(1) @Max(8) size: Int,
    ): InternalTelegramCommentPage {
        val result = comments.listRoot(ListRootCommentsQuery(accountId, PageQuery(page, size)))
        return InternalTelegramCommentPage(
            items = result.items.map { comment ->
                InternalTelegramComment(
                    id = comment.id.id,
                    authorAccountId = comment.authorAccountId,
                    content = if (comment.deleted) null else comment.content,
                    deleted = comment.deleted,
                    likes = comment.likes,
                    replies = comment.replies,
                    createdAt = comment.createdAt,
                )
            },
            page = result.page,
            totalPages = result.totalPages,
            totalItems = result.totalItems,
        )
    }
}
