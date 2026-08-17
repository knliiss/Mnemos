package com.mnemos.realtime.application.broadcast.model

data class NotificationPackBroadcastTemplate(
    val type: String,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)
