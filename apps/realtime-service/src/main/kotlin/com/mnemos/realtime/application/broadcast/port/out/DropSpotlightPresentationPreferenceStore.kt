package com.mnemos.realtime.application.broadcast.port.out

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import java.util.UUID

interface DropSpotlightPresentationPreferenceStore {
    fun find(userId: UUID): DropSpotlightPresentationMode

    fun save(
        userId: UUID,
        mode: DropSpotlightPresentationMode,
    )
}
