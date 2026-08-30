package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.adapter.inbound.http.internal.InternalWipeApiMapper.toContractResponse
import com.mnemos.event.adapter.inbound.http.internal.InternalWipeApiMapper.toResponse
import com.mnemos.event.application.wipe.ListWipesService
import com.mnemos.event.application.wipe.ResolveWipeService
import com.mnemos.event.contracts.internal.v1.InternalWipeListResponse
import com.mnemos.event.domain.wipe.model.WipeId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/wipes")
class InternalWipeController(
    private val listWipesService: ListWipesService,
    private val resolveWipeService: ResolveWipeService,
    private val clock: Clock,
) {
    @GetMapping
    @PreAuthorize("authentication.name == 'telegram-bot-service'")
    fun list(): InternalWipeListResponse {
        val now = clock.instant()
        val wipes = listWipesService.execute()
            .map { wipe -> wipe.toContractResponse(now) }

        return InternalWipeListResponse(wipes = wipes)
    }

    @GetMapping("/current")
    @PreAuthorize("authentication.name == 'bet-service'")
    fun current(): InternalWipeResponse = resolveWipeService.execute(null).toResponse()

    @GetMapping("/{wipeId}")
    @PreAuthorize("authentication.name == 'bet-service'")
    fun get(
        @PathVariable wipeId: UUID,
    ): InternalWipeResponse = resolveWipeService.execute(WipeId(wipeId)).toResponse()
}
