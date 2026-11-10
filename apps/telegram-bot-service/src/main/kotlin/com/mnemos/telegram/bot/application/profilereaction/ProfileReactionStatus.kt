package com.mnemos.telegram.bot.application.profilereaction

enum class ProfileReactionType {
    LIKE,
    DISLIKE,
}

sealed interface ProfileReactionState {
    data class Available(val type: ProfileReactionType?) : ProfileReactionState
    data object AccountRequired : ProfileReactionState
    data object Unavailable : ProfileReactionState
}

enum class ProfileReactionChangeResult {
    APPLIED,
    ACCOUNT_REQUIRED,
    FORBIDDEN,
    INVALID,
    UNAVAILABLE,
}
