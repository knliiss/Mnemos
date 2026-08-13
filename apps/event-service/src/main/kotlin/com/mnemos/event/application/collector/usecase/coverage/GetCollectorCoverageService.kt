package com.mnemos.event.application.collector.usecase.coverage

import com.mnemos.event.application.collector.model.CollectorCoverage
import com.mnemos.event.application.collector.port.out.CollectorCoverageProvider
import org.springframework.stereotype.Service

@Service
class GetCollectorCoverageService(
    private val collectorCoverageProvider: CollectorCoverageProvider,
) : GetCollectorCoverageUseCase {
    override fun execute(query: GetCollectorCoverageQuery): CollectorCoverage =
        collectorCoverageProvider.getCoverage(
            from = query.from,
            to = query.to,
        )
}
