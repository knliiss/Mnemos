package com.mnemos.social.application.comment.usecase.list

import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.domain.comment.model.CommentId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

class ListCommentsServiceTest {
    @Test
    fun `rejects replies lookup for non-root comment`() {
        val reader = mock<CommentViewReader>()
        val service = ListCommentsService(reader)
        val commentId = CommentId(UUID.fromString("019c1ab0-9900-7000-8000-000000000001"))
        val parentId = CommentId(UUID.fromString("019c1ab0-9900-7000-8000-000000000002"))

        whenever(reader.findById(commentId)).thenReturn(commentView(commentId, parentId))

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                service.listReplies(commentId.id, Pageable.unpaged())
            }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    private fun commentView(
        id: CommentId,
        parentId: CommentId?,
    ): CommentView =
        CommentView(
            id = id,
            targetAccountId = UUID.fromString("019c1ab0-9900-7000-8000-000000000010"),
            authorAccountId = UUID.fromString("019c1ab0-9900-7000-8000-000000000011"),
            parentCommentId = parentId,
            content = "content",
            deleted = false,
            likes = 3,
            replies = 0,
            createdAt = Instant.parse("2026-08-16T00:00:00Z"),
            updatedAt = Instant.parse("2026-08-16T00:00:00Z"),
        )
}
