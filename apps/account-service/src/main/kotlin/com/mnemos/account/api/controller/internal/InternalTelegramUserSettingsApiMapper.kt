package com.mnemos.account.api.controller.internal

import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.contracts.account.internal.v1.InternalTelegramLocalizationLanguage
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserSettingsResponse

object InternalTelegramUserSettingsApiMapper {
    fun LocalizationLanguage.toInternal(): InternalTelegramLocalizationLanguage = InternalTelegramLocalizationLanguage.valueOf(name)

    fun InternalTelegramLocalizationLanguage.toDomain(): LocalizationLanguage = LocalizationLanguage.valueOf(name)

    fun LocalizationLanguage.toSettingsResponse(): InternalTelegramUserSettingsResponse =
        InternalTelegramUserSettingsResponse(
            localizationLanguage = toInternal(),
        )
}
