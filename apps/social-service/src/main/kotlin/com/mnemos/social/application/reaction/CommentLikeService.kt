package com.mnemos.social.application.reaction

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.adapter.out.persistence.reaction.CommentLikeEntity
import com.mnemos.social.adapter.out.persistence.reaction.CommentLikeEntityId
import com.mnemos.social.adapter.out.persistence.reaction.CommentLikeJpaRepository
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.domain.comment.model.CommentId
import com.mnemos.social.domain.comment.repository.CommentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class CommentLikeService(
    private val repository: CommentLikeJpaRepository,
    private val commentRepository: CommentRepository,
    private val actorProvider: SocialActorProvider,
    private val clock: Clock,
) {
    @Transactional
    fun like(commentId: UUID) {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_REACTIONS)
        val comment =
            commentRepository.findById(CommentId(commentId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment was not found")

        if (comment.deleted) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Deleted comment cannot be liked")
        }

        val id = CommentLikeEntityId(commentId, actor.accountId)
        if (!repository.existsById(id)) {
            repository.save(
                CommentLikeEntity(
                    commentId = commentId,
                    actorAccountId = actor.accountId,
                    createdAt = clock.instant(),
                ),
            )
        }
    }

    @Transactional
    fun unlike(commentId: UUID) {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_REACTIONS)
        repository.deleteById(CommentLikeEntityId(commentId, actor.accountId))
    }

    @Transactional(readOnly = true)
    fun count(commentId: UUID): Long = repository.countByCommentId(commentId)
}
