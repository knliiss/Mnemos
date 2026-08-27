package com.mnemos.event.application.playeridentity.model

import java.time.Instant
import java.util.UUID

data class PlayerIdentityObservation(
    val playerId: UUID,
    val nickname: String,
    val externalVersion: Long,
    val observedAt: Instant,
) {
    init {
        require(nickname.isNotBlank()) {
            "Player nickname cannot be blank"
        }
        require(externalVersion >= 0) {
            "Player external version cannot be negative"
        }
    }

    val normalizedNickname: String
        get() = normalizeNickname(nickname)
}

fun normalizeNickname(nickname: String): String = nickname.trim().lowercase()
