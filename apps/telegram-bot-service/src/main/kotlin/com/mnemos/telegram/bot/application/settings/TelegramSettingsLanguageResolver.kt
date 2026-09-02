package com.mnemos.telegram.bot.application.settings

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import org.springframework.stereotype.Service

@Service
class TelegramSettingsLanguageResolver(
    private val settingsGateway: SettingsGateway,
) {
    fun resolve(telegramId: Long): TelegramSettingsLanguage =
        try {
            settingsGateway.getSettings(telegramId)?.localizationLanguage ?: DEFAULT_LANGUAGE
        } catch (_: RuntimeException) {
            DEFAULT_LANGUAGE
        }

    private companion object {
        val DEFAULT_LANGUAGE = TelegramSettingsLanguage.RU
    }
}
