package com.mnemos.social.application.comment.usecase.get

import com.mnemos.social.application.comment.exception.CommentNotFoundException
import com.mnemos.social.application.comment.port.out.CommentViewReader
import com.mnemos.social.domain.comment.model.CommentId
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class GetCommentServiceTest {
    @Test
    fun `returns not found for missing comment`() {
        val reader = mock<CommentViewReader>()
        val service = GetCommentService(reader)
        val commentId = CommentId(UUID.fromString("019c1ab0-9900-7000-8000-000000000003"))

        whenever(reader.findById(commentId)).thenReturn(null)

        assertThrows(CommentNotFoundException::class.java) {
            service.execute(commentId.id)
        }
    }
}
