package com.mnemos.playerdirectory.adapter.out.observability

import com.mnemos.playerdirectory.application.observability.port.out.PlayerDirectoryOperationalSnapshotReader
import com.mnemos.playerdirectory.configuration.PlayerDirectoryObservabilityProperties
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

@Component
class PlayerDirectoryOperationalMetrics(
    private val snapshotReader: PlayerDirectoryOperationalSnapshotReader,
    private val properties: PlayerDirectoryObservabilityProperties,
    private val clock: Clock,
    meterRegistry: MeterRegistry,
) {
    private val trackingDueCount = AtomicLong()
    private val oldestTrackingOverdueMillis = AtomicLong()
    private val resolutionQueueCount = AtomicLong()
    private val trackedOnlineCount = AtomicLong()
    private val trackedSaoCount = AtomicLong()
    private val stalePresenceCount = AtomicLong()

    init {
        registerCountGauge(
            meterRegistry = meterRegistry,
            name = "mnemos.player_directory.tracking.due",
            description = "Number of tracked players currently due for polling",
            value = trackingDueCount,
        )
        registerSecondsGauge(
            meterRegistry = meterRegistry,
            name = "mnemos.player_directory.tracking.oldest_overdue",
            description = "Age of the oldest due player tracking poll",
            value = oldestTrackingOverdueMillis,
        )
        registerCountGauge(
            meterRegistry = meterRegistry,
            name = "mnemos.player_directory.resolution.queue",
            description = "Number of unresolved player identity activity records",
            value = resolutionQueueCount,
        )
        registerCountGauge(
            meterRegistry = meterRegistry,
            name = "mnemos.player_directory.tracking.online",
            description = "Number of actively tracked players currently observed online",
            value = trackedOnlineCount,
        )
        registerCountGauge(
            meterRegistry = meterRegistry,
            name = "mnemos.player_directory.tracking.sao",
            description = "Number of actively tracked players currently online in SAO realms",
            value = trackedSaoCount,
        )
        registerCountGauge(
            meterRegistry = meterRegistry,
            name = "mnemos.player_directory.presence.stale",
            description = "Number of actively tracked players with stale presence observations",
            value = stalePresenceCount,
        )
    }

    @Scheduled(
        fixedDelayString = "\${mnemos.player-directory.observability.refresh-interval:15s}",
    )
    fun refresh() {
        val now = Instant.now(clock)
        val snapshot =
            snapshotReader.read(
                now = now,
                staleBefore = now.minus(properties.stalePresenceAfter),
            )

        trackingDueCount.set(snapshot.trackingDueCount)
        resolutionQueueCount.set(snapshot.resolutionQueueCount)
        trackedOnlineCount.set(snapshot.trackedOnlineCount)
        trackedSaoCount.set(snapshot.trackedSaoCount)
        stalePresenceCount.set(snapshot.stalePresenceCount)
        oldestTrackingOverdueMillis.set(
            snapshot.oldestTrackingDueAt
                ?.let { oldestDueAt -> overdueDuration(oldestDueAt, now).toMillis() }
                ?: 0,
        )
    }

    private fun overdueDuration(
        oldestDueAt: Instant,
        now: Instant,
    ): Duration {
        if (!oldestDueAt.isBefore(now)) {
            return Duration.ZERO
        }

        return Duration.between(oldestDueAt, now)
    }

    private fun registerCountGauge(
        meterRegistry: MeterRegistry,
        name: String,
        description: String,
        value: AtomicLong,
    ) {
        Gauge
            .builder(name, value) { current -> current.get().toDouble() }
            .description(description)
            .baseUnit("items")
            .register(meterRegistry)
    }

    private fun registerSecondsGauge(
        meterRegistry: MeterRegistry,
        name: String,
        description: String,
        value: AtomicLong,
    ) {
        Gauge
            .builder(name, value) { current -> current.get().toDouble() / MILLIS_PER_SECOND }
            .description(description)
            .baseUnit("seconds")
            .register(meterRegistry)
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000.0
    }
}
