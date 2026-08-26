package com.mnemos.playerdirectory.application.tracking.usecase.activity

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.time.Instant

fun interface RecordPlayerDropActivityUseCase {
    fun execute(
        playerId: CristalixPlayerId,
        occurredAt: Instant,
    )
}
