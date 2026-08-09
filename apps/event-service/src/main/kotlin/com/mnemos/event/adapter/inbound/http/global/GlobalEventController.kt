package com.mnemos.event.adapter.inbound.http.global

import com.mnemos.event.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.event.adapter.inbound.http.global.GlobalEventApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.global.GlobalEventApiMapper.toResponse
import com.mnemos.event.application.global.usecase.list.ListGlobalEventUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/global-events")
class GlobalEventController(
    private val listGlobalEventUseCase: ListGlobalEventUseCase,
) {
    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListGlobalEventRequest,
    ): PageResponse<GlobalEventResponse> = listGlobalEventUseCase.execute(request.toQuery()).toResponse()
}
