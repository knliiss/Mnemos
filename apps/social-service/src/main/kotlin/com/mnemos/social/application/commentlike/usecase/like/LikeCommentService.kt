package com.mnemos.social.application.commentlike.usecase.like

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import com.mnemos.social.domain.reaction.repository.CommentLikeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class LikeCommentService(
    private val repository: CommentLikeRepository,
    private val commentRepository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val clock: Clock,
) : LikeCommentUseCase {
    @Transactional
    override fun execute(commentId: UUID) {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_REACTIONS)
        val comment =
            commentRepository.findByIdForUpdate(CommentId(commentId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")

        if (comment.deleted) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Deleted comment cannot be liked")
        }

        repository.addIfAbsent(
            commentId = commentId,
            actorAccountId = actor.accountId,
            createdAt = clock.instant(),
        )
    }
}
