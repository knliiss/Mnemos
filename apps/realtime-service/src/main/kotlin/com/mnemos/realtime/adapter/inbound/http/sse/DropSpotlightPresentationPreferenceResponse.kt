package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode

data class DropSpotlightPresentationPreferenceResponse(
    val mode: DropSpotlightPresentationMode,
)
