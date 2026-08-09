package com.mnemos.event.adapter.inbound.http.raid

import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.adapter.inbound.http.raid.RaidEventApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.raid.RaidEventApiMapper.toResponse
import com.mnemos.event.application.raid.usecase.list.ListRaidEventUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/raid-events")
class RaidEventController(
    private val listRaidEventUseCase: ListRaidEventUseCase,
) {
    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListRaidEventRequest,
    ): PageResponse<RaidEventResponse> = listRaidEventUseCase.execute(request.toQuery()).toResponse()
}
