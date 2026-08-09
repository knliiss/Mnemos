package com.mnemos.event.adapter.inbound.http.booster

import com.mnemos.event.adapter.inbound.http.booster.BoosterEventApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.booster.BoosterEventApiMapper.toResponse
import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.application.booster.usecase.list.ListBoosterEventUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/booster-events")
class BoosterEventController(
    private val boosterEventUseCase: ListBoosterEventUseCase,
) {
    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListBoosterEventRequest,
    ): PageResponse<BoosterEventResponse> = boosterEventUseCase.execute(request.toQuery()).toResponse()
}
