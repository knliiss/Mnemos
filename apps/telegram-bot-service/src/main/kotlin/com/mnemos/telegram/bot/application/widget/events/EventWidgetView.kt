package com.mnemos.telegram.bot.application.widget.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import java.util.UUID

data class EventWidgetItem(
    val type: String,
    val label: String,
    val time: String,
)

data class EventWidgetView(
    val filter: EventHistoryFilter,
    val after: UUID?,
    val nextCursor: UUID?,
    val items: List<EventWidgetItem>,
)
