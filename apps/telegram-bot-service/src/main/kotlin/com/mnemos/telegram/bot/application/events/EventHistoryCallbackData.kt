package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import java.util.UUID

object EventHistoryCallbackData {
    const val PREFIX = "ev:"
    const val REFRESH = "ev:r"
    const val PREVIOUS = "ev:p"
    const val SEARCH = "ev:s"
    const val SEARCH_CANCEL = "ev:s:c"
    const val SEARCH_CLEAR = "ev:s:x"

    private const val NEXT_PREFIX = "ev:n:"
    private const val FILTER_PREFIX = "ev:f:"

    fun next(cursor: UUID): String = "$NEXT_PREFIX$cursor"

    fun nextCursor(callbackData: String): UUID? =
        callbackData
            .takeIf { value -> value.startsWith(NEXT_PREFIX) }
            ?.removePrefix(NEXT_PREFIX)
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    fun filter(filter: EventHistoryFilter): String = "$FILTER_PREFIX${filter.name}"

    fun selectedFilter(callbackData: String): EventHistoryFilter? =
        callbackData
            .takeIf { value -> value.startsWith(FILTER_PREFIX) }
            ?.removePrefix(FILTER_PREFIX)
            ?.let { value -> runCatching { EventHistoryFilter.valueOf(value) }.getOrNull() }
}
