package com.mnemos.telegram.bot.application.settings.port.out

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot
import java.util.UUID

interface SettingsGateway {
    fun getSettings(telegramId: Long): TelegramSettingsSnapshot?

    fun updateLanguage(
        telegramId: Long,
        language: TelegramSettingsLanguage,
    ): TelegramSettingsMutationResult

    fun activateNotificationPack(
        telegramId: Long,
        personalizationId: UUID,
    ): TelegramSettingsMutationResult
}
