package com.mnemos.social.adapter.out.persistence.comment

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CommentJpaRepository : JpaRepository<CommentEntity, UUID> {
    fun findByTargetAccountIdAndParentCommentIdIsNull(
        targetAccountId: UUID,
        pageable: Pageable,
    ): Page<CommentEntity>

    fun findByParentCommentId(
        parentCommentId: UUID,
        pageable: Pageable,
    ): Page<CommentEntity>
}
