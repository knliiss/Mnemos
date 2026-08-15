package com.mnemos.social.adapter.out.persistence.reaction

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CommentLikeJpaRepository : JpaRepository<CommentLikeEntity, CommentLikeEntityId> {
    fun countByCommentId(commentId: UUID): Long
}
