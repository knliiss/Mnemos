package com.mnemos.social.adapter.inbound.http.comment

import com.mnemos.social.domain.comment.model.Comment
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateCommentRequest(
    val parentCommentId: UUID?,
    @field:NotBlank
    @field:Size(max = 500)
    val content: String,
)

data class EditCommentRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val content: String,
)

data class CommentResponse(
    val id: UUID,
    val targetAccountId: UUID,
    val authorAccountId: UUID,
    val parentCommentId: UUID?,
    val content: String?,
    val deleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

object CommentApiMapper {
    fun Comment.toResponse(): CommentResponse =
        CommentResponse(
            id = id.id,
            targetAccountId = targetAccountId,
            authorAccountId = authorAccountId,
            parentCommentId = parentCommentId?.id,
            content = content.takeUnless { deleted },
            deleted = deleted,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
