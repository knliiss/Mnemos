package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.adapter.inbound.http.internal.notificationpack.InternalNotificationPackSelectionApiMapper.toTelegramResponse
import com.mnemos.personalization.application.ownership.usecase.getactivepack.TelegramNotificationPackQueryService
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSelectionResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram/{telegramId}/notification-pack-selection")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramNotificationPackSelectionController(
    private val queryService: TelegramNotificationPackQueryService,
) {
    @GetMapping
    fun get(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramNotificationPackSelectionResponse> {
        val selection =
            queryService.findByTelegramId(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(selection.toTelegramResponse())
    }
}
