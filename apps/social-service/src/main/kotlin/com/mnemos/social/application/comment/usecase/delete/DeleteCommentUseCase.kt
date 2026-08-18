package com.mnemos.social.application.comment.usecase.delete

import java.util.UUID

interface DeleteCommentUseCase {
    fun execute(commentId: UUID)
}
