package com.mnemos.event.adapter.out.persistence.booster.query

import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.player.model.CristalixPlayerId

object BoosterEventQueryMapper {
    fun BoosterEventViewProjection.toView(): BoosterEventView =
        BoosterEventView(
            id = EventId(id),
            type = type,
            playerId = playerId?.let(::CristalixPlayerId),
            activatedBy = activatedBy,
            activatedAt = activatedAt,
        )
}
