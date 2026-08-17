package com.mnemos.social.application.commentlike.usecase.unlike

import java.util.UUID

interface UnlikeCommentUseCase {
    fun execute(commentId: UUID)
}
