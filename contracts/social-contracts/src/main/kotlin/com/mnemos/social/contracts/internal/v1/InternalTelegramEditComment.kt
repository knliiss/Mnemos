package com.mnemos.social.contracts.internal.v1

import java.util.UUID

data class InternalTelegramEditCommentRequest(
    val expectedActorAccountId: UUID,
    val content: String,
)

data class InternalTelegramDeleteCommentRequest(
    val expectedActorAccountId: UUID,
)
