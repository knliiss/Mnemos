package com.mnemos.social.domain.comment.model

import java.time.Instant
import java.util.UUID

data class Comment(
    val id: CommentId,
    val targetAccountId: UUID,
    val authorUserId: UUID,
    val authorAccountId: UUID,
    val parentCommentId: CommentId?,
    var content: String,
    val createdAt: Instant,
    var updatedAt: Instant,
    var deletedAt: Instant?,
) {
    val deleted: Boolean
        get() = deletedAt != null

    fun edit(
        newContent: String,
        now: Instant,
    ) {
        require(!deleted) { "Deleted comment cannot be edited" }
        content = normalizeContent(newContent)
        updatedAt = now
    }

    fun delete(now: Instant) {
        if (deletedAt == null) {
            deletedAt = now
            updatedAt = now
        }
    }

    companion object {
        fun create(
            targetAccountId: UUID,
            authorUserId: UUID,
            authorAccountId: UUID,
            parentCommentId: CommentId?,
            content: String,
            now: Instant,
        ): Comment =
            Comment(
                id = CommentId.generate(),
                targetAccountId = targetAccountId,
                authorUserId = authorUserId,
                authorAccountId = authorAccountId,
                parentCommentId = parentCommentId,
                content = normalizeContent(content),
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
            )

        private fun normalizeContent(content: String): String {
            val normalized = content.trim()
            require(normalized.isNotEmpty()) { "Comment content cannot be blank" }
            require(normalized.length <= 500) { "Comment content cannot exceed 500 characters" }
            return normalized
        }
    }
}
