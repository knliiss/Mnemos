package com.mnemos.telegram.bot.application.settings

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import java.util.UUID

object SettingsCallbackData {
    const val PREFIX = "settings:"
    const val REFRESH = "settings:refresh"

    fun language(language: TelegramSettingsLanguage): String = "settings:language:${language.name}"

    fun notificationPack(personalizationId: UUID): String = "settings:pack:$personalizationId"

    fun languageFrom(data: String): TelegramSettingsLanguage? {
        val value = data.removePrefix(LANGUAGE_PREFIX)

        if (value == data) {
            return null
        }

        return runCatching { TelegramSettingsLanguage.valueOf(value) }.getOrNull()
    }

    fun notificationPackIdFrom(data: String): UUID? {
        val value = data.removePrefix(PACK_PREFIX)

        if (value == data) {
            return null
        }

        return runCatching { UUID.fromString(value) }.getOrNull()
    }

    private const val LANGUAGE_PREFIX = "settings:language:"
    private const val PACK_PREFIX = "settings:pack:"
}
