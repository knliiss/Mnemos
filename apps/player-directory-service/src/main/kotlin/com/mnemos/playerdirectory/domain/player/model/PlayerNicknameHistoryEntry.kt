package com.mnemos.playerdirectory.domain.player.model

import java.time.Instant

data class PlayerNicknameHistoryEntry(
    val id: PlayerNicknameHistoryId = PlayerNicknameHistoryId.generate(),
    val playerId: CristalixPlayerId,
    val nickname: PlayerNickname,
    val externalVersion: Long,
    val validFrom: Instant,
    val validUntil: Instant? = null,
) {
    init {
        require(externalVersion >= 0) { "Nickname history external version cannot be negative" }
        require(validUntil == null || !validUntil.isBefore(validFrom)) {
            "Nickname history validity interval cannot end before it starts"
        }
    }

    fun closeAt(observedAt: Instant): PlayerNicknameHistoryEntry =
        copy(
            validUntil = maxOf(validFrom, observedAt),
        )
}
