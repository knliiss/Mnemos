package com.mnemos.telegram.bot.application.events.model

import java.util.UUID

enum class EventHistoryWipeSelection {
    ALL,
    CURRENT,
    PREVIOUS,
}

data class EventHistoryWipeOption(
    val id: UUID,
    val number: Int,
    val name: String?,
    val active: Boolean,
)

data class EventHistoryWipeOptions(
    val current: EventHistoryWipeOption?,
    val previous: EventHistoryWipeOption?,
) {
    fun resolve(selection: EventHistoryWipeSelection): EventHistoryWipeOption? =
        when (selection) {
            EventHistoryWipeSelection.ALL -> null
            EventHistoryWipeSelection.CURRENT -> current
            EventHistoryWipeSelection.PREVIOUS -> previous
        }
}

sealed interface EventHistoryWipeMetadataResult {
    data class Available(
        val options: EventHistoryWipeOptions,
    ) : EventHistoryWipeMetadataResult

    data object Unavailable : EventHistoryWipeMetadataResult
}
