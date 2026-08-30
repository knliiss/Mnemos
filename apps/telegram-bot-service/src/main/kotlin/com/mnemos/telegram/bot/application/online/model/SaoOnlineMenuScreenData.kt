package com.mnemos.telegram.bot.application.online.model

import java.time.Instant

sealed interface SaoOnlineMenuScreenData {
    data class RealmList(
        val snapshot: SaoOnlineSnapshot,
    ) : SaoOnlineMenuScreenData

    data class RealmDetail(
        val generatedAt: Instant,
        val realmName: String,
        val realmNumber: Int,
        val playerCount: Int,
        val page: Int,
        val totalPages: Int,
        val players: List<SaoOnlinePlayer>,
    ) : SaoOnlineMenuScreenData

    data object Unavailable : SaoOnlineMenuScreenData
}
