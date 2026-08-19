package com.mnemos.realtime.application.broadcast.usecase.preference

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationConnectionUpdater
import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationPreferenceStore
import org.springframework.stereotype.Service

@Service
class UpdateDropSpotlightPresentationPreferenceService(
    private val preferenceStore: DropSpotlightPresentationPreferenceStore,
    private val connectionUpdater: DropSpotlightPresentationConnectionUpdater,
) {
    fun execute(command: UpdateDropSpotlightPresentationPreferenceCommand): DropSpotlightPresentationMode {
        preferenceStore.save(
            userId = command.userId,
            mode = command.mode,
        )
        connectionUpdater.update(
            userId = command.userId,
            mode = command.mode,
        )

        return command.mode
    }
}
