package com.mnemos.social.application.comment.usecase.edit

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class EditCommentService(
    private val repository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val clock: Clock,
) : EditCommentUseCase {
    @Transactional
    override fun execute(
        commentId: UUID,
        content: String,
    ): Comment {
        val userId = actorProvider.requireUserId()
        val comment =
            repository.findByIdForUpdate(CommentId(commentId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")

        if (comment.authorUserId != userId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only the author user can edit this comment",
            )
        }

        if (comment.deleted) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Deleted comment cannot be edited")
        }

        if (
            !actorProvider.hasPermission(
                accountId = comment.authorAccountId,
                userId = userId,
                permission = RolePermission.PUBLISH_COMMENTS,
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Account permission is required: ${RolePermission.PUBLISH_COMMENTS}",
            )
        }

        comment.edit(content, clock.instant())

        return repository.save(comment)
    }
}
