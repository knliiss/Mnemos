package com.mnemos.contracts.account.internal.v1

data class InternalTelegramUserSettingsResponse(
    val localizationLanguage: InternalTelegramLocalizationLanguage,
)

data class UpdateInternalTelegramUserLocalizationRequest(
    val localizationLanguage: InternalTelegramLocalizationLanguage,
)

enum class InternalTelegramLocalizationLanguage {
    EN,
    RU,
    UK,
}
