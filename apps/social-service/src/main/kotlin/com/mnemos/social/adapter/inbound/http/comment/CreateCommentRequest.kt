package com.mnemos.social.adapter.inbound.http.comment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateCommentRequest(
    val parentCommentId: UUID?,
    @field:NotBlank
    @field:Size(max = 500)
    val content: String,
)
