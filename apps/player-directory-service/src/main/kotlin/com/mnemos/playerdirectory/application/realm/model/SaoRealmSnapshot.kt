package com.mnemos.playerdirectory.application.realm.model

import java.time.Instant

data class SaoRealmSnapshot(
    val generatedAt: Instant,
    val trackedOnline: Int,
    val realms: List<SaoRealm>,
)

data class SaoRealm(
    val name: SaoRealmName,
    val players: List<SaoRealmPlayer>,
)
