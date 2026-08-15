package com.mnemos.realtime.adapter.inbound.scheduling.sse

import com.mnemos.realtime.adapter.sse.connection.SseConnectionRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SseHeartbeatScheduler(
    private val connectionRegistry: SseConnectionRegistry,
) {
    @Scheduled(fixedDelayString = "\${mnemos.realtime.delivery.heartbeat-interval:15s}")
    fun heartbeat() {
        connectionRegistry.heartbeat()
    }
}
