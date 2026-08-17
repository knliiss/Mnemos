package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import jakarta.validation.constraints.NotNull

data class UpdateDropSpotlightPresentationPreferenceRequest(
    @field:NotNull
    val mode: DropSpotlightPresentationMode,
)
