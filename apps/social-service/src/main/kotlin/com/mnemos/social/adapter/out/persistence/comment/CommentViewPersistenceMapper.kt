package com.mnemos.social.adapter.out.persistence.comment

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.domain.comment.model.CommentId

object CommentViewPersistenceMapper {
    fun CommentViewProjection.toView(): CommentView =
        CommentView(
            id = CommentId(id),
            targetAccountId = targetAccountId,
            authorAccountId = authorAccountId,
            parentCommentId = parentCommentId?.let(::CommentId),
            content = content,
            deleted = deleted,
            likes = likes,
            replies = replies,
            createdAt = createdAt,
            updatedAt = updatedAt,
            authorUserId = authorUserId,
            replyToAuthorAccountId = replyToAuthorAccountId,
        )
}
