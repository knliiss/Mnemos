package com.mnemos.realtime.adapter.sse.connection

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationConnectionUpdater
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SseDropSpotlightPresentationConnectionUpdater(
    private val connectionRegistry: SseConnectionRegistry,
) : DropSpotlightPresentationConnectionUpdater {
    override fun update(
        userId: UUID,
        mode: DropSpotlightPresentationMode,
    ) {
        connectionRegistry.updateDropSpotlightPresentationMode(
            userId = userId,
            mode = mode,
        )
    }
}
