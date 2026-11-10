package com.mnemos.telegram.bot.application.widget.invites

interface InviteWidgetSnapshotQuery {
    fun load(telegramId: Long, page: Int): InviteWidgetSnapshotResult
}
