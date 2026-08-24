package com.mnemos.event.adapter.inbound.http.wipe

import com.mnemos.event.adapter.inbound.http.wipe.WipeApiMapper.toResponse
import com.mnemos.event.application.wipe.StartWipeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events/admin/wipes")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminWipeController(
    private val startWipeService: StartWipeService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun start(
        @RequestBody
        @Valid
        request: StartWipeRequest,
        authentication: Authentication,
    ): WipeResponse =
        startWipeService
            .execute(
                name = request.name,
                startedAt = request.startedAt,
                createdBy = authentication.name,
            ).toResponse()
}
