package com.mnemos.playerdirectory.adapter.out.http.cristalix

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class CristalixApiMetrics(
    private val meterRegistry: MeterRegistry,
) {
    fun recordBatchSize(
        operation: String,
        size: Int,
    ) {
        meterRegistry
            .summary(
                "mnemos.player_directory.cristalix.batch_size",
                "operation",
                operation,
            ).record(size.toDouble())
    }
}
