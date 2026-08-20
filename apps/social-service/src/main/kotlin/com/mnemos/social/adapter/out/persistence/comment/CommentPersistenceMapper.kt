package com.mnemos.social.adapter.out.persistence.comment

import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId

object CommentPersistenceMapper {
    fun Comment.toEntity(): CommentEntity =
        CommentEntity(
            id = id.id,
            targetAccountId = targetAccountId,
            authorUserId = authorUserId,
            authorAccountId = authorAccountId,
            parentCommentId = parentCommentId?.id,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            replyToCommentId = replyToCommentId?.id,
        )

    fun CommentEntity.toDomain(): Comment =
        Comment(
            id = CommentId(id),
            targetAccountId = targetAccountId,
            authorUserId = authorUserId,
            authorAccountId = authorAccountId,
            parentCommentId = parentCommentId?.let(::CommentId),
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            replyToCommentId = replyToCommentId?.let(::CommentId),
        )
}
