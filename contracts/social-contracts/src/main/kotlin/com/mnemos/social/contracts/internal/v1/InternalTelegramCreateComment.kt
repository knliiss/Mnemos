package com.mnemos.social.contracts.internal.v1

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class InternalTelegramCreateCommentRequest(
    val operationId: UUID,
    val expectedActorAccountId: UUID,
    val parentCommentId: UUID?,
    @field:NotBlank
    @field:Size(max = 500)
    val content: String,
)

data class InternalTelegramCreateCommentResponse(
    val commentId: UUID,
)
