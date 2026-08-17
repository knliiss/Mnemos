package com.mnemos.social.application.commentlike.usecase.like

import java.util.UUID

interface LikeCommentUseCase {
    fun execute(commentId: UUID)
}
