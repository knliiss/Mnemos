package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.adapter.inbound.http.comment.CommentApiMapper.toResponse
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.domain.comment.model.CommentId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CommentApiMapperTest {
    @Test
    fun `response exposes edit and delete capabilities for the comment author`() {
        val currentUserId = UUID.randomUUID()
        val view = commentView(authorUserId = currentUserId)

        val response = view.toResponse(currentUserId)

        assertTrue(response.canEdit)
        assertTrue(response.canDelete)
    }

    @Test
    fun `response exposes delete capability to an account moderator`() {
        val view = commentView(authorUserId = UUID.randomUUID())

        val response =
            view.toResponse(
                currentUserId = UUID.randomUUID(),
                canModerateAccount = true,
            )

        assertFalse(response.canEdit)
        assertTrue(response.canDelete)
    }

    @Test
    fun `response does not expose author capabilities to another user`() {
        val view = commentView(authorUserId = UUID.randomUUID())

        val response = view.toResponse(UUID.randomUUID())

        assertFalse(response.canEdit)
        assertFalse(response.canDelete)
    }

    @Test
    fun `response does not expose edit or delete actions for a deleted comment`() {
        val currentUserId = UUID.randomUUID()
        val view =
            commentView(
                authorUserId = currentUserId,
                deleted = true,
            )

        val response =
            view.toResponse(
                currentUserId = currentUserId,
                canModerateAccount = true,
            )

        assertFalse(response.canEdit)
        assertFalse(response.canDelete)
    }

    private fun commentView(
        authorUserId: UUID,
        deleted: Boolean = false,
    ): CommentView {
        val now = Instant.parse("2026-08-20T12:00:00Z")

        return CommentView(
            id = CommentId.generate(),
            targetAccountId = UUID.randomUUID(),
            authorAccountId = UUID.randomUUID(),
            parentCommentId = null,
            content = if (deleted) null else "Comment",
            deleted = deleted,
            likes = 0,
            replies = 0,
            createdAt = now,
            updatedAt = now,
            authorUserId = authorUserId,
            replyToAuthorAccountId = null,
        )
    }
}
