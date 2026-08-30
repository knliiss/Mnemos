package com.mnemos.telegram.bot.application.online.model

import java.time.Instant

data class SaoOnlineSnapshot(
    val generatedAt: Instant,
    val trackedOnline: Int,
    val realms: List<SaoOnlineRealm>,
)
