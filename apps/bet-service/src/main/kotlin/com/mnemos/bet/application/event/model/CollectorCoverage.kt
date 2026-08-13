package com.mnemos.bet.application.event.model

data class CollectorCoverage(
    val covered: Boolean,
    val maximumActiveCollectors: Int,
)
