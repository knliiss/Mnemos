package com.mnemos.telegram.bot.application.widget.profileactivity

import java.util.UUID

data class ProfileActivityView(
    val profileId: UUID,
    val nickname: String,
    val after: UUID?,
    val nextCursor: UUID?,
    val items: List<ProfileActivityItem>,
)
