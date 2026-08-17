package com.mnemos.event.adapter.out.persistence.raid.query

import com.mnemos.event.application.raid.model.RaidEventView
import com.mnemos.event.domain.EventId

object RaidEventQueryMapper {
    fun RaidEventViewProjection.toView(): RaidEventView =
        RaidEventView(
            id = EventId(id),
            locations = locations,
            startedAt = startedAt,
        )
}
