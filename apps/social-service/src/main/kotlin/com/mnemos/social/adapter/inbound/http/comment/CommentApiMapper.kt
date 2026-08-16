package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.domain.comment.model.Comment

object CommentApiMapper {
    fun Comment.toResponse(): CommentResponse =
        CommentResponse(
            id = id.id,
            targetAccountId = targetAccountId,
            authorAccountId = authorAccountId,
            parentCommentId = parentCommentId?.id,
            content = content.takeUnless { deleted },
            deleted = deleted,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
