package com.mnemos.bet.adapter.out.event

data class CollectorCoverageResponse(
    val covered: Boolean,
    val maximumActiveCollectors: Int,
)
