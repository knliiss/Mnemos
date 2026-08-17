package com.mnemos.realtime.application.broadcast.model

data class NotificationPackBroadcastSnapshot(
    val mixedAggregateTitle: String,
    val templates: List<NotificationPackBroadcastTemplate>,
)
