package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.adapter.inbound.http.internal.notificationpack.InternalTelegramNotificationPackApiMapper.toResponse
import com.mnemos.personalization.application.ownership.usecase.notificationpack.TelegramNotificationPackManagementService
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackListResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram/{telegramId}/notification-packs")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramNotificationPackController(
    private val service: TelegramNotificationPackManagementService,
) {
    @GetMapping
    fun list(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramNotificationPackListResponse> {
        val packs =
            service.listOwned(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            InternalTelegramNotificationPackListResponse(
                packs = packs.map { pack -> pack.toResponse() },
            ),
        )
    }
}
