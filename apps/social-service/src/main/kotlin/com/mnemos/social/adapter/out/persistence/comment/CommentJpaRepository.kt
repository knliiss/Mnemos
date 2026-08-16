package com.mnemos.social.adapter.out.persistence.comment

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CommentJpaRepository : JpaRepository<CommentEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select comment from CommentEntity comment where comment.id = :commentId")
    fun findForUpdate(@Param("commentId") commentId: UUID): CommentEntity?

    @Query(
        """
            SELECT comment
            FROM CommentEntity comment
            WHERE comment.targetAccountId = :targetAccountId
              AND comment.parentCommentId IS NULL
            ORDER BY comment.createdAt DESC, comment.id DESC
        """,
    )
    fun findRootByAccountId(
        @Param("targetAccountId") targetAccountId: UUID,
        pageable: Pageable,
    ): Page<CommentEntity>

    @Query(
        """
            SELECT comment
            FROM CommentEntity comment
            WHERE comment.parentCommentId = :parentCommentId
            ORDER BY comment.createdAt ASC, comment.id ASC
        """,
    )
    fun findReplies(
        @Param("parentCommentId") parentCommentId: UUID,
        pageable: Pageable,
    ): Page<CommentEntity>
}
