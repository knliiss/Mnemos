package com.mnemos.telegram.bot.contracts.internal.bet

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TelegramEventBetRoundResponse(
    val id: UUID,
    val eventType: String,
    val scheduledAt: Instant,
    val closesAt: Instant,
    val resolvesAt: Instant,
    val yesOdds: BigDecimal,
    val noOdds: BigDecimal,
    val status: String,
    val outcome: String?,
    val myBet: TelegramEventBetResponse?,
)
