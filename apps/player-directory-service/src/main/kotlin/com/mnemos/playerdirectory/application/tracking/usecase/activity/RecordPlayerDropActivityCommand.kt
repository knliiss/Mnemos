package com.mnemos.playerdirectory.application.tracking.usecase.activity

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.time.Instant

data class RecordPlayerDropActivityCommand(
    val playerId: CristalixPlayerId,
    val occurredAt: Instant,
    val dropCount: Long = 1,
) {
    init {
        require(dropCount > 0) {
            "Recorded drop count must be positive"
        }
    }
}
