package com.mnemos.notification.adapter.out.message.local

import com.mnemos.notification.application.notification.model.NotificationType

data class ResolvedNotification(
    val type: NotificationType,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String? = null,
)
