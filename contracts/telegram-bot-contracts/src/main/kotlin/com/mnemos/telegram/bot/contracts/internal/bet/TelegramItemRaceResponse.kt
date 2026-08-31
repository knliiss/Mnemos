package com.mnemos.telegram.bot.contracts.internal.bet

import java.time.Instant
import java.util.UUID

data class TelegramItemRaceResponse(
    val id: UUID,
    val itemName: String,
    val itemType: String,
    val registrationClosesAt: Instant,
    val startsAt: Instant,
    val expiresAt: Instant,
    val entryAmount: Long,
    val feePercent: Int,
    val status: String,
    val winnerAccountId: UUID?,
    val myPosition: TelegramItemRaceParticipantResponse?,
)
