package com.mnemos.personalization.adapter.inbound.http.personalization.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NotificationValueFormsRequest(
    @field:NotBlank
    @field:Size(max = 128)
    val default: String,
    @field:Size(max = 8)
    val forms: Map<String, String> = emptyMap(),
)
