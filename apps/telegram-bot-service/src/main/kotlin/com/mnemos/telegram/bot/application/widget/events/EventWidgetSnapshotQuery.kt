package com.mnemos.telegram.bot.application.widget.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import java.util.UUID

interface EventWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        filter: EventHistoryFilter,
        after: UUID?,
        search: String? = null,
    ): EventWidgetSnapshotResult
}
