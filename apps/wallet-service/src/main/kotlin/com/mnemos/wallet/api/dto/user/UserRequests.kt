package com.mnemos.accountservice.api.dto.user

import com.mnemos.common.core.model.user.LocalizationLanguage
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateUsernameRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 20)
    val username: String,
)

data class UpdateLocalizationRequest(
    val localizationLanguage: LocalizationLanguage,
)
