package com.mnemos.event.application.collector.usecase.coverage

import java.time.Instant

data class GetCollectorCoverageQuery(
    val from: Instant,
    val to: Instant,
)
