package com.mnemos.telegram.bot.application.widget.management

fun interface AccountManagementWidgetSnapshotQuery {
    fun load(telegramId: Long, page: Int): AccountManagementWidgetSnapshotResult
}
