package com.mnemos.bet.contracts.event

import com.mnemos.bet.contracts.shared.BetResult
import java.time.Instant
import java.util.UUID

data class ItemRaceSettledV1(
    val raceId: UUID,
    val accountId: UUID,
    val result: BetResult,
    val amount: Long,
    val payout: Long,
    val settledAt: Instant,
)
