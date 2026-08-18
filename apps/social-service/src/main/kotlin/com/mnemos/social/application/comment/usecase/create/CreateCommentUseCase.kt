package com.mnemos.social.application.comment.usecase.create

import com.mnemos.social.domain.comment.model.Comment
import java.util.UUID

interface CreateCommentUseCase {
    fun execute(
        targetAccountId: UUID,
        parentCommentId: UUID?,
        content: String,
    ): Comment
}
