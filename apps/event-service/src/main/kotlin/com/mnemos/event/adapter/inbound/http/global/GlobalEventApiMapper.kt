package com.mnemos.event.adapter.inbound.http.global

import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.adapter.inbound.http.common.pagination.toPageResponse
import com.mnemos.event.application.common.pagination.PageQuery
import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.global.model.GlobalEventView
import com.mnemos.event.application.global.usecase.list.ListGlobalEventQuery

object GlobalEventApiMapper {
    fun ListGlobalEventRequest.toQuery(): ListGlobalEventQuery =
        ListGlobalEventQuery(
            types = types,
            page =
                PageQuery(
                    page = page,
                    size = size,
                ),
        )

    fun PageResult<GlobalEventView>.toResponse(): PageResponse<GlobalEventResponse> = toPageResponse { it.toResponse() }

    private fun GlobalEventView.toResponse(): GlobalEventResponse =
        GlobalEventResponse(
            id = id.id,
            type = type,
            startedAt = startedAt,
        )
}
