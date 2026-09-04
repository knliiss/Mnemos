package com.mnemos.account.api.dto.user

import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateUsernameRequest(
    @field:NotBlank
    @field:Size(min = Username.MIN_LENGTH, max = Username.MAX_LENGTH)
    val username: String,
)

data class UpdateLocalizationRequest(
    val localizationLanguage: LocalizationLanguage,
)
