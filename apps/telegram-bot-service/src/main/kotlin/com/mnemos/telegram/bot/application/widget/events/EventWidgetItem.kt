package com.mnemos.telegram.bot.application.widget.events

import java.util.UUID

data class EventWidgetItem(
    val type: String,
    val label: String,
    val time: String,
    val eventId: UUID? = null,
)

