package com.mnemos.event.adapter.inbound.collector.dto

data class CollectorFieldViolation(
    val field: String,
    val message: String,
)
