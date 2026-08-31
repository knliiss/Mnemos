package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramUserIdentityApiMapper.toIdentityResponse
import com.mnemos.account.application.service.user.InternalTelegramUserBootstrapService
import com.mnemos.account.application.service.user.InternalTelegramUserQueryService
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram")
@PreAuthorize(
    "authentication.name == 'personalization-service' or " +
        "authentication.name == 'payment-service' or " +
        "authentication.name == 'notification-service'",
)
class InternalTelegramUserIdentityController(
    private val service: InternalTelegramUserQueryService,
    private val bootstrapService: InternalTelegramUserBootstrapService,
) {
    @GetMapping("/{telegramId}/identity")
    fun getIdentity(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramUserIdentityResponse> {
        val context =
            service.findByTelegramId(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(context.toIdentityResponse())
    }

    @PostMapping("/{telegramId}/identity/bootstrap")
    @PreAuthorize(
        "authentication.name == 'payment-service' or " +
            "authentication.name == 'notification-service'",
    )
    fun bootstrapIdentity(
        @PathVariable telegramId: Long,
    ): InternalTelegramUserIdentityResponse {
        val user = bootstrapService.getOrCreate(telegramId)

        return InternalTelegramUserIdentityResponse(userId = user.id.id)
    }
}
