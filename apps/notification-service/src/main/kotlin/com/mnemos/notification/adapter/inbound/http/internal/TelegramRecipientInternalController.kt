package com.mnemos.notification.adapter.inbound.http.internal

import com.mnemos.notification.application.recipient.ActivateTelegramRecipientService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/recipients")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramRecipientInternalController(
    private val service: ActivateTelegramRecipientService,
) {
    @PostMapping("/{telegramId}/activation")
    fun activate(@PathVariable telegramId: Long) {
        service.execute(telegramId)
    }
}
