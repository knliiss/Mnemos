package com.mnemos.social.application.comment.port.out

import com.mnemos.social.application.comment.query.CommentView
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CommentViewReader {
    fun findById(id: CommentId): CommentView?

    fun findRootByAccountId(
        accountId: UUID,
        pageable: Pageable,
    ): Page<CommentView>

    fun findReplies(
        parentCommentId: CommentId,
        pageable: Pageable,
    ): Page<CommentView>
}
