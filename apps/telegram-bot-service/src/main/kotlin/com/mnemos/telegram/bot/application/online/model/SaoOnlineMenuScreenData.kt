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

    data class SearchPrompt(
        val error: String?,
    ) : SaoOnlineMenuScreenData

    data class SearchResults(
        val generatedAt: Instant,
        val query: String,
        val totalMatches: Int,
        val page: Int,
        val totalPages: Int,
        val matches: List<SaoOnlinePlayerSearchMatch>,
    ) : SaoOnlineMenuScreenData

    data object Unavailable : SaoOnlineMenuScreenData
}
