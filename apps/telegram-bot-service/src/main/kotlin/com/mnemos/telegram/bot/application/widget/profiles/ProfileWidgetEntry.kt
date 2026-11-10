package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import java.util.UUID

data class ProfileWidgetEntry(
    val accountId: UUID,
    val nickname: String,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val presenceStatus: ProfilePresenceStatus,
    val realm: String?,
    val lastSeenOnline: String?,
    val joinedAt: String,
)
