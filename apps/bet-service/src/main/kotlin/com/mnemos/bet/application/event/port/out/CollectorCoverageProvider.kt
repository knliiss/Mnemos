package com.mnemos.bet.application.event.port.out

import com.mnemos.bet.application.event.model.CollectorCoverage
import java.time.Instant

interface CollectorCoverageProvider {
    fun getCoverage(
        from: Instant,
        to: Instant,
    ): CollectorCoverage
}
