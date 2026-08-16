package com.mnemos.social.adapter.out.persistence.comment

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CommentViewJpaRepository : Repository<CommentEntity, UUID> {
    @Query(
        value = """
            SELECT c.id AS "id",
                   c.target_account_id AS "targetAccountId",
                   c.author_account_id AS "authorAccountId",
                   c.parent_comment_id AS "parentCommentId",
                   CASE WHEN c.deleted_at IS NULL THEN c.content ELSE NULL END AS "content",
                   (c.deleted_at IS NOT NULL) AS "deleted",
                   CASE
                       WHEN c.deleted_at IS NULL THEN (
                           SELECT count(*)
                           FROM comment_likes comment_like
                           WHERE comment_like.comment_id = c.id
                       )
                       ELSE 0::bigint
                   END AS "likes",
                   (
                       SELECT count(*)
                       FROM comments reply
                       WHERE reply.parent_comment_id = c.id
                   ) AS "replies",
                   c.created_at AS "createdAt",
                   c.updated_at AS "updatedAt"
            FROM comments c
            WHERE c.id = :commentId
        """,
        nativeQuery = true,
    )
    fun findViewById(@Param("commentId") commentId: UUID): CommentViewProjection?

    @Query(
        value = """
            SELECT c.id AS "id",
                   c.target_account_id AS "targetAccountId",
                   c.author_account_id AS "authorAccountId",
                   c.parent_comment_id AS "parentCommentId",
                   CASE WHEN c.deleted_at IS NULL THEN c.content ELSE NULL END AS "content",
                   (c.deleted_at IS NOT NULL) AS "deleted",
                   CASE
                       WHEN c.deleted_at IS NULL THEN (
                           SELECT count(*)
                           FROM comment_likes comment_like
                           WHERE comment_like.comment_id = c.id
                       )
                       ELSE 0::bigint
                   END AS "likes",
                   (
                       SELECT count(*)
                       FROM comments reply
                       WHERE reply.parent_comment_id = c.id
                   ) AS "replies",
                   c.created_at AS "createdAt",
                   c.updated_at AS "updatedAt"
            FROM comments c
            WHERE c.target_account_id = :targetAccountId
              AND c.parent_comment_id IS NULL
            ORDER BY c.created_at DESC, c.id DESC
        """,
        countQuery = """
            SELECT count(*)
            FROM comments c
            WHERE c.target_account_id = :targetAccountId
              AND c.parent_comment_id IS NULL
        """,
        nativeQuery = true,
    )
    fun findRootByAccountId(
        @Param("targetAccountId") targetAccountId: UUID,
        pageable: Pageable,
    ): Page<CommentViewProjection>

    @Query(
        value = """
            SELECT c.id AS "id",
                   c.target_account_id AS "targetAccountId",
                   c.author_account_id AS "authorAccountId",
                   c.parent_comment_id AS "parentCommentId",
                   CASE WHEN c.deleted_at IS NULL THEN c.content ELSE NULL END AS "content",
                   (c.deleted_at IS NOT NULL) AS "deleted",
                   CASE
                       WHEN c.deleted_at IS NULL THEN (
                           SELECT count(*)
                           FROM comment_likes comment_like
                           WHERE comment_like.comment_id = c.id
                       )
                       ELSE 0::bigint
                   END AS "likes",
                   0::bigint AS "replies",
                   c.created_at AS "createdAt",
                   c.updated_at AS "updatedAt"
            FROM comments c
            WHERE c.parent_comment_id = :parentCommentId
            ORDER BY c.created_at ASC, c.id ASC
        """,
        countQuery = """
            SELECT count(*)
            FROM comments c
            WHERE c.parent_comment_id = :parentCommentId
        """,
        nativeQuery = true,
    )
    fun findReplies(
        @Param("parentCommentId") parentCommentId: UUID,
        pageable: Pageable,
    ): Page<CommentViewProjection>
}
