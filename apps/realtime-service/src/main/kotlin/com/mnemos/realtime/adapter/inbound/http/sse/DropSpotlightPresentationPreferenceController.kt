package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.application.broadcast.usecase.preference.GetDropSpotlightPresentationPreferenceService
import com.mnemos.realtime.application.broadcast.usecase.preference.UpdateDropSpotlightPresentationPreferenceCommand
import com.mnemos.realtime.application.broadcast.usecase.preference.UpdateDropSpotlightPresentationPreferenceService
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
    private val getPreferenceService: GetDropSpotlightPresentationPreferenceService,
    private val updatePreferenceService: UpdateDropSpotlightPresentationPreferenceService,
) {
    @GetMapping
    fun get(): DropSpotlightPresentationPreferenceResponse =
        DropSpotlightPresentationPreferenceResponse(
            mode = getPreferenceService.execute(currentUser.requireUserId()),
        )

    @PutMapping
    fun update(
        @Valid @RequestBody request: UpdateDropSpotlightPresentationPreferenceRequest,
    ): DropSpotlightPresentationPreferenceResponse {
        val mode =
            updatePreferenceService.execute(
                UpdateDropSpotlightPresentationPreferenceCommand(
                    userId = currentUser.requireUserId(),
                    mode = request.mode,
                ),
            )

        return DropSpotlightPresentationPreferenceResponse(
            mode = mode,
        )
    }
}
