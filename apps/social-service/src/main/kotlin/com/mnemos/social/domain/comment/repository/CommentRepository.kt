package com.mnemos.social.domain.comment.repository

import com.mnemos.social.domain.comment.model.Comment
import com.mnemos.social.domain.comment.model.CommentId

interface CommentRepository {
    fun save(comment: Comment): Comment

    fun findById(id: CommentId): Comment?

    fun findByIdForUpdate(id: CommentId): Comment?
}
