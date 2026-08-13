package com.mnemos.event.adapter.out.memory.collector

import com.mnemos.event.application.collector.model.CollectorCoverage
import com.mnemos.event.application.collector.port.out.CollectorCoverageProvider
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.ArrayDeque

@Component
class CollectorCoverageTracker(
    private val clock: Clock,
) : CollectorCoverageProvider {
    private val startedAt: Instant = clock.instant()
    private val snapshots = ArrayDeque<CollectorActivitySnapshot>()

    init {
        snapshots.addLast(
            CollectorActivitySnapshot(
                occurredAt = startedAt,
                activeCollectors = 0,
            ),
        )
    }

    @Synchronized
    fun record(activeCollectors: Int) {
        val now = clock.instant()
        val last = snapshots.lastOrNull()

        if (last?.activeCollectors == activeCollectors) {
            return
        }

        snapshots.addLast(
            CollectorActivitySnapshot(
                occurredAt = now,
                activeCollectors = activeCollectors,
            ),
        )

        trim(now)
    }

    @Synchronized
    override fun getCoverage(
        from: Instant,
        to: Instant,
    ): CollectorCoverage {
        require(!to.isBefore(from))

        if (from.isBefore(startedAt)) {
            return CollectorCoverage(
                covered = false,
                maximumActiveCollectors = 0,
            )
        }

        val current = snapshots.toList()
        val activeAtStart =
            current
                .lastOrNull { snapshot -> !snapshot.occurredAt.isAfter(from) }
                ?.activeCollectors
                ?: 0

        val maximumInWindow =
            current
                .asSequence()
                .filter { snapshot -> snapshot.occurredAt.isAfter(from) && !snapshot.occurredAt.isAfter(to) }
                .map(CollectorActivitySnapshot::activeCollectors)
                .fold(activeAtStart, ::maxOf)

        return CollectorCoverage(
            covered = maximumInWindow > 0,
            maximumActiveCollectors = maximumInWindow,
        )
    }

    private fun trim(now: Instant) {
        val cutoff = now.minus(RETENTION)

        while (snapshots.size > 1) {
            val second = snapshots.elementAt(1)

            if (!second.occurredAt.isBefore(cutoff)) {
                break
            }

            snapshots.removeFirst()
        }
    }

    private companion object {
        val RETENTION: Duration = Duration.ofHours(24)
    }
}
