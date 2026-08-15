package com.mnemos.event.application.collector.port.out

import java.time.Instant

interface CollectorCoverageRecorder {
    fun record(
        activeCollectors: Int,
        occurredAt: Instant,
        revision: Long,
    )
}
