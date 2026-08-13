package com.mnemos.event.application.collector.usecase.coverage

import com.mnemos.event.application.collector.model.CollectorCoverage

interface GetCollectorCoverageUseCase {
    fun execute(query: GetCollectorCoverageQuery): CollectorCoverage
}
