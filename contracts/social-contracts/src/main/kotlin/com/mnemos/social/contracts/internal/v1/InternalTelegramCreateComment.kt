package com.mnemos.social.contracts.internal.v1

import java.util.UUID

data class InternalTelegramCreateCommentRequest(
    val operationId: UUID,
    val expectedActorAccountId: UUID,
    val parentCommentId: UUID?,
    val content: String,
)

data class InternalTelegramCreateCommentResponse(
    val commentId: UUID,
)
