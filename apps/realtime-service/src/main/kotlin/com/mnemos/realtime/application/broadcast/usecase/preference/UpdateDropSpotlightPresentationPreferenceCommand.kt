package com.mnemos.realtime.application.broadcast.usecase.preference

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import java.util.UUID

data class UpdateDropSpotlightPresentationPreferenceCommand(
    val userId: UUID,
    val mode: DropSpotlightPresentationMode,
)
