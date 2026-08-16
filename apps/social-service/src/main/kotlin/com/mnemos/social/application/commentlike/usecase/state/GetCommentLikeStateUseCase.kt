package com.mnemos.social.application.commentlike.usecase.state

import java.util.UUID

interface GetCommentLikeStateUseCase {
    fun execute(commentId: UUID): Boolean
}
