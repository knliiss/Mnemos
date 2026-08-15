package com.mnemos.realtime.application.delivery.port.out

import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import java.time.Duration

interface RealtimeDeliveryTelemetry {
    fun recordPublishSuccess(
        event: RealtimeEvent,
        duration: Duration,
    )

    fun recordPublishFailure(
        event: RealtimeEvent,
        duration: Duration,
    )

    fun recordStreamDelivery(event: RealtimeEvent)

    fun connectionOpened()

    fun connectionClosed()

    fun recordSseSendFailure()
}
