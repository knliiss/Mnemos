package com.mnemos.telegram.bot.contracts.internal.bet

import java.util.UUID

data class TelegramIncreaseItemRaceRequest(
    val amount: Long,
    val idempotencyKey: UUID,
)
