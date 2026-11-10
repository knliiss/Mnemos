package com.mnemos.telegram.bot.application.events.model

sealed interface EventDetailResult {
    data class Available(val item: EventHistoryItem) : EventDetailResult
    data object NotFound : EventDetailResult
    data object Unavailable : EventDetailResult
}
