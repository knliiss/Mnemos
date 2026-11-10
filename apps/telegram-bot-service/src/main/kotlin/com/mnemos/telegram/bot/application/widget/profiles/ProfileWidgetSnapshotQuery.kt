package com.mnemos.telegram.bot.application.widget.profiles

import java.util.UUID

interface ProfileWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        query: String?,
        page: Int,
        selectedProfileId: UUID? = null,
    ): ProfileWidgetSnapshotResult
}
