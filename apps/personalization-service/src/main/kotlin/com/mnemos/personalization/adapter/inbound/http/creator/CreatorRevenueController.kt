package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.creator.CreatorRevenueApiMapper.toResponse
import com.mnemos.personalization.application.revenue.query.CreatorRevenueQueryService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/personalizations/creator/revenue")
@PreAuthorize("hasAnyRole('PERSONALIZATION_CREATOR', 'ADMIN', 'SUPER_ADMIN')")
class CreatorRevenueController(
    private val queryService: CreatorRevenueQueryService,
) {
    @GetMapping("/summary")
    fun summary(): CreatorRevenueSummaryResponse = queryService.getSummary().toResponse()

    @GetMapping("/operations")
    fun operations(): List<CreatorRevenueOperationResponse> = queryService.getOperations().map { operation -> operation.toResponse() }
}
