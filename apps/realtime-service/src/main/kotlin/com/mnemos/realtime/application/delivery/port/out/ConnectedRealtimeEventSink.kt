package com.mnemos.realtime.application.delivery.port.out

import com.mnemos.realtime.application.delivery.model.RealtimeEvent

interface ConnectedRealtimeEventSink {
    fun send(event: RealtimeEvent)
}
