package com.mnemos.telegram.bot.contracts.internal.bet

import java.util.UUID

data class TelegramEventBetResponse(
    val id: UUID,
    val roundId: UUID,
    val side: TelegramBetSide,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: String,
)
