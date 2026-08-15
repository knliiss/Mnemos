package com.mnemos.social.domain.comment.repository

import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CommentRepository {
    fun save(comment: Comment): Comment

    fun findById(id: CommentId): Comment?

    fun findRootByAccountId(
        accountId: UUID,
        pageable: Pageable,
    ): Page<Comment>

    fun findReplies(
        parentCommentId: CommentId,
        pageable: Pageable,
    ): Page<Comment>
}
