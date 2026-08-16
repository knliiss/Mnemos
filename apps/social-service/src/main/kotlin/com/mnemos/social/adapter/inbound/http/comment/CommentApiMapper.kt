package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.application.comment.model.CommentView

object CommentApiMapper {
    fun CommentView.toResponse(): CommentResponse =
        CommentResponse(
            id = id.id,
            targetAccountId = targetAccountId,
            authorAccountId = authorAccountId,
            parentCommentId = parentCommentId?.id,
            content = content,
            deleted = deleted,
            likes = likes,
            replies = replies,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
