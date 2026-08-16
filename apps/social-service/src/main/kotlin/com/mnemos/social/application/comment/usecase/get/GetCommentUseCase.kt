package com.mnemos.social.application.comment.usecase.get

import com.mnemos.social.application.comment.model.CommentView
import java.util.UUID

interface GetCommentUseCase {
    fun execute(commentId: UUID): CommentView
}
