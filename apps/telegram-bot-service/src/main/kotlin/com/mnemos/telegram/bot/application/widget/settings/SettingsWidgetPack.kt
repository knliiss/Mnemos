package com.mnemos.telegram.bot.application.widget.settings

import java.util.UUID

data class SettingsWidgetPack(
    val id: UUID,
    val title: String,
    val active: Boolean,
)
