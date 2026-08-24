package com.mnemos.event.adapter.inbound.http.wipe

import com.mnemos.event.adapter.inbound.http.wipe.WipeApiMapper.toResponse
import com.mnemos.event.application.wipe.ListWipesService
import com.mnemos.event.application.wipe.ResolveWipeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events/wipes")
class WipeController(
    private val listWipesService: ListWipesService,
    private val resolveWipeService: ResolveWipeService,
) {
    @GetMapping
    fun list(): List<WipeResponse> = listWipesService.execute().map { wipe -> wipe.toResponse() }

    @GetMapping("/current")
    fun current(): WipeResponse = resolveWipeService.execute(null).toResponse()
}
