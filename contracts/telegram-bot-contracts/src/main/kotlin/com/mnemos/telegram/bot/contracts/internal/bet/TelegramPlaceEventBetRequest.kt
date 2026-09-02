package com.mnemos.telegram.bot.contracts.internal.bet

import java.util.UUID

data class TelegramPlaceEventBetRequest(
    val side: TelegramBetSide,
    val amount: Long,
    val idempotencyKey: UUID,
)
