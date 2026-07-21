package com.mnemos.accountservice.api.dto.user

import com.mnemos.common.core.model.user.LocalizationLanguage

data class UpdateLocalizationRequest(
    val localizationLanguage: LocalizationLanguage
)
