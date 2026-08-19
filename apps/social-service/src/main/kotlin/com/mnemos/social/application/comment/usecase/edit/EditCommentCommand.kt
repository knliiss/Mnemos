package com.mnemos.social.application.comment.usecase.edit

import com.mnemos.social.domain.comment.model.CommentId

data class EditCommentCommand(
    val commentId: CommentId,
    val content: String,
)
