package com.mnemos.social.adapter.out.persistence.reaction

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface CommentLikeJpaRepository : JpaRepository<CommentLikeEntity, CommentLikeEntityId> {
    @Modifying
    @Query(
        value = """
            INSERT INTO comment_likes (comment_id, actor_account_id, created_at)
            VALUES (:commentId, :actorAccountId, :createdAt)
            ON CONFLICT (comment_id, actor_account_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun addIfAbsent(
        @Param("commentId") commentId: UUID,
        @Param("actorAccountId") actorAccountId: UUID,
        @Param("createdAt") createdAt: Instant,
    ): Int

    @Modifying
    @Query(
        value = """
            DELETE FROM comment_likes
            WHERE comment_id = :commentId
              AND actor_account_id = :actorAccountId
        """,
        nativeQuery = true,
    )
    fun remove(
        @Param("commentId") commentId: UUID,
        @Param("actorAccountId") actorAccountId: UUID,
    ): Int

    fun countByCommentId(commentId: UUID): Long
}
