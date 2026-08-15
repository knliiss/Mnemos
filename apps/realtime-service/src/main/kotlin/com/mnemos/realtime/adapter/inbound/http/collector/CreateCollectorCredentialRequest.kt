package com.mnemos.realtime.adapter.inbound.http.collector

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateCollectorCredentialRequest(
    @field:NotBlank
    @field:Size(max = 64)
    val name: String,
)
