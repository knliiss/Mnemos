package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.adapter.sse.connection.SseConnectionRegistry
import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationPreferenceStore
import com.mnemos.security.CurrentMnemosUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/realtime/drop-spotlight/presentation")
class DropSpotlightPresentationPreferenceController(
    private val currentUser: CurrentMnemosUser,
    private val preferenceStore: DropSpotlightPresentationPreferenceStore,
    private val connectionRegistry: SseConnectionRegistry,
) {
    @GetMapping
    fun get(): DropSpotlightPresentationPreferenceResponse {
        val userId = currentUser.requireUserId()

        return DropSpotlightPresentationPreferenceResponse(
            mode = preferenceStore.find(userId),
        )
    }

    @PutMapping
    fun update(
        @Valid @RequestBody request: UpdateDropSpotlightPresentationPreferenceRequest,
    ): DropSpotlightPresentationPreferenceResponse {
        val userId = currentUser.requireUserId()

        preferenceStore.save(
            userId = userId,
            mode = request.mode,
        )
        connectionRegistry.updateDropSpotlightPresentationMode(
            userId = userId,
            mode = request.mode,
        )

        return DropSpotlightPresentationPreferenceResponse(
            mode = request.mode,
        )
    }
}
