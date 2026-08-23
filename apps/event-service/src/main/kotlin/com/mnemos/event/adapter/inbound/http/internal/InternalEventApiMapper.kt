package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.collector.model.CollectorCoverage
import com.mnemos.event.application.collector.usecase.coverage.GetCollectorCoverageQuery
import com.mnemos.event.application.itemdrop.model.FirstItemDropView
import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropQuery

object InternalEventApiMapper {
    fun FindFirstItemDropRequest.toQuery(): FindFirstItemDropQuery =
        FindFirstItemDropQuery(
            itemName = itemName,
            itemType = itemType,
            from = from,
            to = to,
        )

    fun CollectorCoverageRequest.toQuery(): GetCollectorCoverageQuery =
        GetCollectorCoverageQuery(
            from = from,
            to = to,
        )

    fun FirstItemDropView.toFirstResponse(): FirstItemDropResponse =
        FirstItemDropResponse(
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )

    fun CollectorCoverage.toResponse(): CollectorCoverageResponse =
        CollectorCoverageResponse(
            covered = covered,
            maximumActiveCollectors = maximumActiveCollectors,
        )
}
