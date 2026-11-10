package com.mnemos.telegram.bot.application.widget.profileactivity

import java.util.UUID

interface ProfileActivitySnapshotQuery {
    fun load(
        telegramId: Long,
        profileId: UUID,
        after: UUID? = null,
    ): ProfileActivitySnapshotResult
}
