package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.adapter.inbound.http.internal.InternalWipeApiMapper.toResponse
import com.mnemos.event.application.wipe.ResolveWipeService
import com.mnemos.event.domain.wipe.model.WipeId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/wipes")
@PreAuthorize("authentication.name == 'bet-service'")
class InternalWipeController(
    private val resolveWipeService: ResolveWipeService,
) {
    @GetMapping("/current")
    fun current(): InternalWipeResponse = resolveWipeService.execute(null).toResponse()

    @GetMapping("/{wipeId}")
    fun get(
        @PathVariable wipeId: UUID,
    ): InternalWipeResponse = resolveWipeService.execute(WipeId(wipeId)).toResponse()
}
