package com.mnemos.bet.contracts.event

import com.mnemos.bet.contracts.shared.BetGlobalEventType
import java.time.Instant
import java.util.UUID

data class EventBetOpenedV1(
    val roundId: UUID,
    val eventType: BetGlobalEventType,
    val scheduledAt: Instant,
    val closesAt: Instant,
)
