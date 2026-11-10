package com.mnemos.telegram.bot.application.widget.profilecomments

import java.util.UUID

interface ProfileCommentsWidgetSnapshotQuery {
    fun load(telegramId: Long, profileId: UUID, page: Int, rootCommentId: UUID? = null): ProfileCommentsWidgetSnapshotResult
}
