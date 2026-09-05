package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramUserSettingsApiMapper.toInternal
import com.mnemos.account.application.service.user.InternalUserLocalizationService
import com.mnemos.common.core.model.id.UserId
import com.mnemos.contracts.account.internal.v1.InternalUserLocalizationResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/users/{userId}/localization")
@PreAuthorize("authentication.name == 'notification-service'")
class InternalUserLocalizationController(
    private val service: InternalUserLocalizationService,
) {
    @GetMapping
    fun getLocalization(
        @PathVariable userId: UUID,
    ): ResponseEntity<InternalUserLocalizationResponse> {
        val localization =
            service.findLocalization(UserId(userId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            InternalUserLocalizationResponse(
                localizationLanguage = localization.toInternal(),
            ),
        )
    }
}
