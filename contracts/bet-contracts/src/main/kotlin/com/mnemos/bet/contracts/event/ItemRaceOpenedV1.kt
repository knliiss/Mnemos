package com.mnemos.bet.contracts.event

import com.mnemos.bet.contracts.shared.BetItemType
import java.time.Instant
import java.util.UUID

data class ItemRaceOpenedV1(
    val raceId: UUID,
    val itemName: String,
    val itemType: BetItemType,
    val registrationClosesAt: Instant,
    val startsAt: Instant,
    val expiresAt: Instant,
    val entryAmount: Long,
)
