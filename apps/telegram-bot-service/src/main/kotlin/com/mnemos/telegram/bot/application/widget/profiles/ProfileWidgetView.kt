package com.mnemos.telegram.bot.application.widget.profiles

data class ProfileWidgetView(
    val query: String?,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
    val profiles: List<ProfileWidgetEntry>,
    val selected: ProfileWidgetEntry? = null,
)
