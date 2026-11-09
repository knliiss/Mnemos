package com.mnemos.telegram.bot.application.widget.notifications

import java.util.UUID

data class NotificationWidgetDrop(
    val id: UUID,
    val label: String,
)

data class NotificationWidgetView(
    val accountId: UUID?,
    val page: Int,
    val totalPages: Int,
    val totalDrops: Long,
    val pingEnabled: Boolean,
    val itemDrops: List<NotificationWidgetDrop>,
    val boosters: Int,
    val dungeons: Int,
    val globalEvents: Int,
    val raidEnabled: Boolean,
)
