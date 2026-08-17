package com.mnemos.event.adapter.out.persistence.booster.query

import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.domain.EventId

object BoosterEventQueryMapper {
    fun BoosterEventViewProjection.toView(): BoosterEventView =
        BoosterEventView(
            id = EventId(id),
            type = type,
            activatedBy = activatedBy,
            activatedAt = activatedAt,
        )
}
