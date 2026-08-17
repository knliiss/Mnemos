package com.mnemos.event.adapter.inbound.http.booster

import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponseMapper.toPageResponse
import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.application.booster.usecase.list.ListBoosterEventQuery
import com.mnemos.event.application.common.pagination.PageQuery
import com.mnemos.event.application.common.pagination.PageResult

object BoosterEventApiMapper {
    fun ListBoosterEventRequest.toQuery(): ListBoosterEventQuery =
        ListBoosterEventQuery(
            type = type,
            activatedByName = activatedByName,
            page =
                PageQuery(
                    page = page,
                    size = size,
                ),
        )

    fun PageResult<BoosterEventView>.toResponse(): PageResponse<BoosterEventResponse> = toPageResponse { it.toResponse() }

    private fun BoosterEventView.toResponse(): BoosterEventResponse =
        BoosterEventResponse(
            id = id.id,
            type = type,
            activatedBy = activatedBy,
            activatedAt = activatedAt,
        )
}
