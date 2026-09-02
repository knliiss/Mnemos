package com.mnemos.telegram.bot.application.online.model

data class SaoOnlineRealm(
    val name: String,
    val number: Int,
    val playerCount: Int,
    val players: List<SaoOnlinePlayer>,
)
