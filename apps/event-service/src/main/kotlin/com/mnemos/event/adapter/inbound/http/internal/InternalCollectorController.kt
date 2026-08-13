package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.collector.usecase.coverage.GetCollectorCoverageQuery
import com.mnemos.event.application.collector.usecase.coverage.GetCollectorCoverageUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/internal/v1/collectors")
class InternalCollectorController(
    private val getCollectorCoverageUseCase: GetCollectorCoverageUseCase,
) {
    @GetMapping("/coverage")
    fun coverage(
        @RequestParam
        from: Instant,
        @RequestParam
        to: Instant,
    ): CollectorCoverageResponse {
        val coverage =
            getCollectorCoverageUseCase.execute(
                GetCollectorCoverageQuery(
                    from = from,
                    to = to,
                ),
            )

        return CollectorCoverageResponse(
            covered = coverage.covered,
            maximumActiveCollectors = coverage.maximumActiveCollectors,
        )
    }
}
