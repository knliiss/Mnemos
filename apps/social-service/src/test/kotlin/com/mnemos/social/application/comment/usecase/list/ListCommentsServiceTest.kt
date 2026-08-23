package com.mnemos.social.application.comment.usecase.list

import com.mnemos.social.application.comment.exception.CommentRepliesRequireRootException
import com.mnemos.social.application.comment.model.CommentView
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.application.common.pagination.PageQuery
import com.mnemos.social.domain.comment.model.CommentId
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class ListCommentsServiceTest {
    @Test
    fun `rejects replies lookup for non-root comment`() {
        val reader = mock<CommentViewReader>()
        val service = ListCommentsService(reader)
        val commentId = CommentId(UUID.fromString("019c1ab0-9900-7000-8000-000000000001"))
        val parentId = CommentId(UUID.fromString("019c1ab0-9900-7000-8000-000000000002"))
        val query =
            ListCommentRepliesQuery(
                commentId = commentId,
                page =
                    PageQuery(
                        page = 0,
                        size = 20,
                    ),
            )

        whenever(reader.findById(commentId)).thenReturn(commentView(commentId, parentId))

        assertThrows(CommentRepliesRequireRootException::class.java) {
            service.listReplies(query)
        }
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
