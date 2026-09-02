package com.mnemos.telegram.bot.application.settings.model

import java.util.UUID

data class TelegramSettingsSnapshot(
    val localizationLanguage: TelegramSettingsLanguage?,
    val activeNotificationPackId: UUID?,
    val activeNotificationPackTitle: String?,
    val notificationPacks: List<TelegramNotificationPackOption>,
)

data class TelegramNotificationPackOption(
    val personalizationId: UUID,
    val title: String,
    val active: Boolean,
)

enum class TelegramSettingsLanguage {
    EN,
    RU,
    UK,
}

enum class TelegramSettingsMutationResult {
    UPDATED,
    UNAVAILABLE,
}
