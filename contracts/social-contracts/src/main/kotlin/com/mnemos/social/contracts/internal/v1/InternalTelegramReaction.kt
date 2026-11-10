package com.mnemos.social.contracts.internal.v1

enum class InternalTelegramReactionType {
    LIKE,
    DISLIKE,
}

data class InternalTelegramReactionRequest(
    val type: InternalTelegramReactionType,
)

data class InternalTelegramReactionState(
    val type: InternalTelegramReactionType?,
)
