package com.mnemos.telegram.bot.application.widget.eventdetail

data class EventDetailView(
    val type: String,
    val title: String,
    val occurredAt: String,
    val facts: List<EventDetailFact>,
)
