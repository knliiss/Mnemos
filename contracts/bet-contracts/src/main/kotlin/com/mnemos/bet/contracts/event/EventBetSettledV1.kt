package com.mnemos.bet.contracts.event

import com.mnemos.bet.contracts.shared.BetGlobalEventType
import com.mnemos.bet.contracts.shared.BetResult
import com.mnemos.bet.contracts.shared.BetSide
import java.time.Instant
import java.util.UUID

data class EventBetSettledV1(
    val roundId: UUID,
    val accountId: UUID,
    val eventType: BetGlobalEventType,
    val side: BetSide,
    val amount: Long,
    val result: BetResult,
    val payout: Long,
    val settledAt: Instant,
)
