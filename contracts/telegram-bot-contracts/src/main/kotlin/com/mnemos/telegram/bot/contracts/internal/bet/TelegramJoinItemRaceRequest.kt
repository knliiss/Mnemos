package com.mnemos.telegram.bot.contracts.internal.bet

import java.util.UUID

data class TelegramJoinItemRaceRequest(
    val idempotencyKey: UUID,
)
