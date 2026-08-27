package com.mnemos.event.application.booster.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.player.model.CristalixPlayerId
import java.time.Instant

data class BoosterEventView(
    val id: EventId,
    val type: DomainBoosterType,
    val playerId: CristalixPlayerId?,
    val activatedBy: String,
    val activatedAt: Instant,
)
