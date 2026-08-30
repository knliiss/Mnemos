package com.mnemos.telegram.bot.application.settings.port.out

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot

fun interface SettingsGateway {
    fun getSettings(telegramId: Long): TelegramSettingsSnapshot?
}
