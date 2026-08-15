package com.mnemos.realtime.support

import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.port.out.RealtimeDeliveryTelemetry
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventFanout
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventReplayStore
import java.time.Duration
import java.util.UUID

class RecordingRealtimeEventSink :
    RealtimeEventReplayStore,
    RealtimeEventFanout,
    RealtimeDeliveryTelemetry {
    val persistedEvents = mutableListOf<RealtimeEvent>()
    val broadcastEvents = mutableListOf<RealtimeEvent>()

    override fun append(event: RealtimeEvent) {
        persistedEvents += event
    }

    override fun findAfter(
        userId: UUID,
        lastEventId: UUID,
    ): List<RealtimeEvent> = emptyList()

    override fun publish(event: RealtimeEvent) {
        broadcastEvents += event
    }

    override fun recordPublishSuccess(
        event: RealtimeEvent,
        duration: Duration,
    ) = Unit

    override fun recordPublishFailure(
        event: RealtimeEvent,
        duration: Duration,
    ) = Unit

    override fun recordStreamDelivery(event: RealtimeEvent) = Unit

    override fun connectionOpened() = Unit

    override fun connectionClosed() = Unit

    override fun recordSseSendFailure() = Unit
}
