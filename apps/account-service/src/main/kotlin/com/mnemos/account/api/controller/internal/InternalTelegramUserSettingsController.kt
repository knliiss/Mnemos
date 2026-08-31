package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramUserSettingsApiMapper.toDomain
import com.mnemos.account.api.controller.internal.InternalTelegramUserSettingsApiMapper.toSettingsResponse
import com.mnemos.account.application.service.user.InternalTelegramUserSettingsService
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserSettingsResponse
import com.mnemos.contracts.account.internal.v1.UpdateInternalTelegramUserLocalizationRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram/{telegramId}/settings")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramUserSettingsController(
    private val service: InternalTelegramUserSettingsService,
) {
    @GetMapping
    fun getSettings(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramUserSettingsResponse> {
        val localization =
            service.findLocalization(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(localization.toSettingsResponse())
    }

    @PutMapping("/localization")
    fun updateLocalization(
        @PathVariable telegramId: Long,
        @RequestBody request: UpdateInternalTelegramUserLocalizationRequest,
    ): ResponseEntity<InternalTelegramUserSettingsResponse> {
        val localization =
            service.updateLocalization(
                telegramId = TelegramId(telegramId),
                localizationLanguage = request.localizationLanguage.toDomain(),
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(localization.toSettingsResponse())
    }
}
