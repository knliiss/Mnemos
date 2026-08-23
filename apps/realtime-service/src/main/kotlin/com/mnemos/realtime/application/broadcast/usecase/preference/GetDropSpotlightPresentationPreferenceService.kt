package com.mnemos.realtime.application.broadcast.usecase.preference

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationPreferenceStore
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetDropSpotlightPresentationPreferenceService(
    private val preferenceStore: DropSpotlightPresentationPreferenceStore,
) {
    fun execute(userId: UUID): DropSpotlightPresentationMode = preferenceStore.find(userId)
}
