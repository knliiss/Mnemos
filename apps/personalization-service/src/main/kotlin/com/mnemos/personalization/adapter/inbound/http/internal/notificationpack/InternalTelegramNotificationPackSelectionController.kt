package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.adapter.inbound.http.internal.notificationpack.InternalNotificationPackSelectionApiMapper.toTelegramResponse
import com.mnemos.personalization.application.ownership.usecase.getactivepack.TelegramNotificationPackQueryService
import com.mnemos.personalization.application.ownership.usecase.notificationpack.TelegramNotificationPackManagementService
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSelectionResponse
import com.mnemos.personalization.contracts.internal.v1.UpdateInternalTelegramNotificationPackSelectionRequest
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram/{telegramId}/notification-pack-selection")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramNotificationPackSelectionController(
    private val queryService: TelegramNotificationPackQueryService,
    private val managementService: TelegramNotificationPackManagementService,
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

    @PutMapping
    fun update(
        @PathVariable telegramId: Long,
        @RequestBody request: UpdateInternalTelegramNotificationPackSelectionRequest,
    ): ResponseEntity<InternalTelegramNotificationPackSelectionResponse> {
        val telegramUserId = TelegramId(telegramId)
        val activated =
            managementService.activate(
                telegramId = telegramUserId,
                personalizationId = PersonalizationId(request.personalizationId),
            )

        if (!activated) {
            return ResponseEntity.notFound().build()
        }

        val selection =
            queryService.findByTelegramId(telegramUserId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(selection.toTelegramResponse())
    }
}
