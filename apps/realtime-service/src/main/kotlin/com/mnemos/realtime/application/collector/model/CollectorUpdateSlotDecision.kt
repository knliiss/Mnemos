package com.mnemos.realtime.application.collector.model

data class CollectorUpdateSlotDecision(
    val granted: Boolean,
    val retryAfterSeconds: Long?,
)
