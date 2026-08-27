package com.mnemos.playerdirectory.domain.player.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class PlayerNicknameHistoryId(
    val id: UUID,
) {
    companion object {
        fun generate(): PlayerNicknameHistoryId = PlayerNicknameHistoryId(UuidCreator.getTimeOrderedEpoch())
    }
}
