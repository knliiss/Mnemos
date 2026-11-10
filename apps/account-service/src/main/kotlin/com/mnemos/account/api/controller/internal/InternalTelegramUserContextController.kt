package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramUserContextApiMapper.toResponse
import com.mnemos.account.application.service.user.InternalTelegramUserQueryService
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserContextResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram")
@PreAuthorize("authentication.name == 'reward-service' or authentication.name == 'social-service'")
class InternalTelegramUserContextController(
    private val service: InternalTelegramUserQueryService,
) {
    @GetMapping("/{telegramId}/context")
    fun getContext(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramUserContextResponse> {
        val context =
            service.findByTelegramId(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(context.toResponse())
    }
}
