package com.mnemos.social.application.comment.usecase.list

import com.mnemos.social.application.comment.model.CommentView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ListCommentsUseCase {
    fun listRoot(
        accountId: UUID,
        pageable: Pageable,
    ): Page<CommentView>

    fun listReplies(
        commentId: UUID,
        pageable: Pageable,
    ): Page<CommentView>
}
