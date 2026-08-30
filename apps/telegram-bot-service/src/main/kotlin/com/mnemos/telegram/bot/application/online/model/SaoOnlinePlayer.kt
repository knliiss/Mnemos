package com.mnemos.telegram.bot.application.online.model

import java.time.Instant
import java.util.UUID

data class SaoOnlinePlayer(
    val playerId: UUID,
    val nickname: String,
    val accountId: UUID?,
    val observedAt: Instant,
)
