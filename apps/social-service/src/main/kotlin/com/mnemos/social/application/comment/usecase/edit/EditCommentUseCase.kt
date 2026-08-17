package com.mnemos.social.application.comment.usecase.edit

import com.mnemos.social.domain.comment.model.Comment
import java.util.UUID

interface EditCommentUseCase {
    fun execute(
        commentId: UUID,
        content: String,
    ): Comment
}
