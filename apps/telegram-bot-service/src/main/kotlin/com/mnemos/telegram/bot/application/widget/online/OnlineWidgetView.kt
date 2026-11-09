package com.mnemos.telegram.bot.application.widget.online

data class OnlineWidgetView(
    val trackedOnline: Int,
    val totalRealms: Int,
    val selectedRealmNumber: Int?,
    val selectedRealmName: String?,
    val selectedRealmPlayerCount: Int?,
    val generatedAt: String,
    val realms: List<OnlineWidgetRealm>,
    val players: List<OnlineWidgetPlayer>,
)
