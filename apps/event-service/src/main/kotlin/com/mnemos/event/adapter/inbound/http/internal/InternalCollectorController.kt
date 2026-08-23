package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.adapter.inbound.http.internal.InternalEventApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.internal.InternalEventApiMapper.toResponse
import com.mnemos.event.application.collector.usecase.coverage.GetCollectorCoverageUseCase
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/collectors")
@PreAuthorize("authentication.name == 'bet-service'")
class InternalCollectorController(
    private val getCollectorCoverageUseCase: GetCollectorCoverageUseCase,
) {
    @GetMapping("/coverage")
    fun coverage(
        @Valid @ModelAttribute request: CollectorCoverageRequest,
    ): CollectorCoverageResponse = getCollectorCoverageUseCase.execute(request.toQuery()).toResponse()
}
