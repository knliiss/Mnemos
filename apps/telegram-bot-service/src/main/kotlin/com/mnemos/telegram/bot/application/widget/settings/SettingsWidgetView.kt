package com.mnemos.telegram.bot.application.widget.settings

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import java.util.UUID

data class SettingsWidgetView(
    val language: TelegramSettingsLanguage?,
    val activePackId: UUID?,
    val activePackTitle: String?,
    val packs: List<SettingsWidgetPack>,
    val totalPacks: Int,
    val page: Int,
    val totalPages: Int,
)
