package com.mnemos.social.adapter.out.persistence.reaction

import com.mnemos.social.domain.reaction.repository.CommentLikeRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class CommentLikeRepositoryAdapter(
    private val repository: CommentLikeJpaRepository,
) : CommentLikeRepository {
    override fun addIfAbsent(
        commentId: UUID,
        actorAccountId: UUID,
        createdAt: Instant,
    ): Boolean =
        repository.addIfAbsent(
            commentId = commentId,
            actorAccountId = actorAccountId,
            createdAt = createdAt,
        ) > 0

    override fun remove(
        commentId: UUID,
        actorAccountId: UUID,
    ): Boolean =
        repository.remove(
            commentId = commentId,
            actorAccountId = actorAccountId,
        ) > 0

    override fun countByCommentId(commentId: UUID): Long = repository.countByCommentId(commentId)
}
