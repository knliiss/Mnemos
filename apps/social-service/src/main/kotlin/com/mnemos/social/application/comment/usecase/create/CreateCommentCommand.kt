package com.mnemos.social.application.comment.usecase.create

import com.mnemos.social.domain.comment.model.CommentId
import java.util.UUID

data class CreateCommentCommand(
    val targetAccountId: UUID,
    val parentCommentId: CommentId?,
    val content: String,
)
