package com.mnemos.event.application.collector.model

data class CollectorCoverage(
    val covered: Boolean,
    val maximumActiveCollectors: Int,
)
