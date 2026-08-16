package com.mnemos.social.domain.reaction.repository

import java.time.Instant
import java.util.UUID

interface CommentLikeRepository {
    fun addIfAbsent(
        commentId: UUID,
        actorAccountId: UUID,
        createdAt: Instant,
    ): Boolean

    fun remove(
        commentId: UUID,
        actorAccountId: UUID,
    ): Boolean

    fun exists(
        commentId: UUID,
        actorAccountId: UUID,
    ): Boolean

    fun countByCommentId(commentId: UUID): Long
}
