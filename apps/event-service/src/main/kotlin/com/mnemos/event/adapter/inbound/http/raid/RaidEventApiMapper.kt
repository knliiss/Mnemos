package com.mnemos.event.adapter.inbound.http.raid

import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.adapter.inbound.http.common.pagination.toPageResponse
import com.mnemos.event.application.common.pagination.PageQuery
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.raid.model.RaidEventView
import com.mnemos.event.application.raid.usecase.list.ListRaidEventQuery

object RaidEventApiMapper {
    fun ListRaidEventRequest.toQuery(): ListRaidEventQuery =
        ListRaidEventQuery(
            minLocation = minLocation,
            maxLocation = maxLocation,
            page =
                PageQuery(
                    page = page,
                    size = size,
                ),
        )

    fun PageResult<RaidEventView>.toResponse(): PageResponse<RaidEventResponse> = toPageResponse { it.toResponse() }

    private fun RaidEventView.toResponse(): RaidEventResponse =
        RaidEventResponse(
            id = id.id,
            locations = locations,
            startedAt = startedAt,
        )
}
