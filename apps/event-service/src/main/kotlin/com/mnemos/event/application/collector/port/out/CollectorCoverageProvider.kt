package com.mnemos.event.application.collector.port.out

import com.mnemos.event.application.collector.model.CollectorCoverage
import java.time.Instant

interface CollectorCoverageProvider {
    fun getCoverage(
        from: Instant,
        to: Instant,
    ): CollectorCoverage
}
