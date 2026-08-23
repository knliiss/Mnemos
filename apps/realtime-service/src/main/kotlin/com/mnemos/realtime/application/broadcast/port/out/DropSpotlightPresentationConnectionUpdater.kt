package com.mnemos.realtime.application.broadcast.port.out

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import java.util.UUID

interface DropSpotlightPresentationConnectionUpdater {
    fun update(
        userId: UUID,
        mode: DropSpotlightPresentationMode,
    )
}
