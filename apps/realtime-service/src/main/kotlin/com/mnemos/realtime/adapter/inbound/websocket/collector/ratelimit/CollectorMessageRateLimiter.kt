package com.mnemos.realtime.adapter.inbound.websocket.collector.ratelimit

import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class CollectorMessageRateLimiter(
    private val properties: CollectorConnectionProperties,
    private val clock: Clock,
) {
    private val windows = ConcurrentHashMap<CollectorCredentialId, RateWindow>()

    fun allow(collectorId: CollectorCredentialId): Boolean {
        val now = clock.instant()

        val window =
            windows.compute(collectorId) { _, current ->
                if (current == null || !now.isBefore(current.startedAt.plusSeconds(1))) {
                    RateWindow(
                        startedAt = now,
                        messages = 1,
                    )
                } else {
                    current.copy(messages = current.messages + 1)
                }
            }

        return requireNotNull(window).messages <= properties.messagesPerSecond
    }

    fun remove(collectorId: CollectorCredentialId) {
        windows.remove(collectorId)
    }

    private data class RateWindow(
        val startedAt: Instant,
        val messages: Int,
    )
}
