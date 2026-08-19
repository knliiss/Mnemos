package com.mnemos.social.application.commentlike.usecase.like

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.comment.exception.CommentNotFoundException
import com.mnemos.social.application.comment.exception.DeletedCommentOperationException
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import com.mnemos.social.domain.reaction.repository.CommentLikeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        val id = CommentId(commentId)
        val comment = commentRepository.findByIdForUpdate(id)
            ?: throw CommentNotFoundException(id)

        if (comment.deleted) {
            throw DeletedCommentOperationException(
                commentId = id,
                operation = "liked",
            )
        }

        repository.addIfAbsent(
            commentId = commentId,
            actorAccountId = actor.accountId,
            createdAt = clock.instant(),
        )
    }
}
