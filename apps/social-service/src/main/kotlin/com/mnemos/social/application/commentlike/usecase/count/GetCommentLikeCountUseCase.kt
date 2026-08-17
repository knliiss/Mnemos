package com.mnemos.social.application.commentlike.usecase.count

import java.util.UUID

interface GetCommentLikeCountUseCase {
    fun execute(commentId: UUID): Long
}
