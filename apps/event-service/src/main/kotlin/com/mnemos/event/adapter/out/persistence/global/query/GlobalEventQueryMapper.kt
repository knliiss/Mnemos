package com.mnemos.event.adapter.out.persistence.global.query

import com.mnemos.event.application.global.model.GlobalEventView
import com.mnemos.event.domain.EventId

object GlobalEventQueryMapper {
    fun GlobalEventViewProjection.toView(): GlobalEventView =
        GlobalEventView(
            id = EventId(id),
            type = type,
            startedAt = startedAt,
        )
}
