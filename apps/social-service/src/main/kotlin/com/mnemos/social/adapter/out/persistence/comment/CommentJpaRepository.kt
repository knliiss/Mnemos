package com.mnemos.social.adapter.out.persistence.comment

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CommentJpaRepository : JpaRepository<CommentEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select comment from CommentEntity comment where comment.id = :commentId")
    fun findForUpdate(
        @Param("commentId") commentId: UUID,
    ): CommentEntity?
}
