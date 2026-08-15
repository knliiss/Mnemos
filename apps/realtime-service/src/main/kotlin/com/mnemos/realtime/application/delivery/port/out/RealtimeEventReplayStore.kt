package com.mnemos.realtime.application.delivery.port.out

import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import java.util.UUID

interface RealtimeEventReplayStore {
    fun append(event: RealtimeEvent)

    fun findAfter(
        userId: UUID,
        lastEventId: UUID,
    ): List<RealtimeEvent>
}
