package com.mnemos.realtime.adapter.inbound.websocket.collector.ingress

import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class CollectorInFlightMessageLimiter(
    private val properties: CollectorConnectionProperties,
) {
    private val counters = ConcurrentHashMap<CollectorCredentialId, AtomicInteger>()

    fun tryAcquire(collectorId: CollectorCredentialId): Boolean {
        val counter = counters.computeIfAbsent(collectorId) { AtomicInteger() }
        val inFlight = counter.incrementAndGet()

        if (inFlight <= properties.maxInFlightMessages) {
            return true
        }

        release(
            collectorId = collectorId,
            counter = counter,
        )

        return false
    }

    fun release(collectorId: CollectorCredentialId) {
        val counter = counters[collectorId] ?: return

        release(
            collectorId = collectorId,
            counter = counter,
        )
    }

    private fun release(
        collectorId: CollectorCredentialId,
        counter: AtomicInteger,
    ) {
        if (counter.decrementAndGet() == 0) {
            counters.remove(collectorId, counter)
        }
    }
}
