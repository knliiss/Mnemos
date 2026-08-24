package com.mnemos.event.adapter.inbound.http.wipe

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class StartWipeRequest(
    @field:Size(max = 64)
    val name: String? = null,
    @field:NotNull
    val startedAt: Instant,
)
