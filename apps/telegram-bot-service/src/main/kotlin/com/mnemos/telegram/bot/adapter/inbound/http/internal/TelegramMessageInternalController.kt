package com.mnemos.telegram.bot.adapter.inbound.http.internal

import com.mnemos.telegram.bot.adapter.inbound.http.internal.TelegramMessageApiMapper.toDomain
import com.mnemos.telegram.bot.adapter.inbound.http.internal.TelegramMessageApiMapper.toResponse
import com.mnemos.telegram.bot.application.message.DeliverTelegramMessageService
import com.mnemos.telegram.bot.contracts.internal.message.EditTelegramMessageRequest
import com.mnemos.telegram.bot.contracts.internal.message.SendTelegramMessageRequest
import com.mnemos.telegram.bot.contracts.internal.message.TelegramDeliveryResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/messages")
@PreAuthorize("authentication.name == 'notification-service'")
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramMessageInternalController(
    private val service: DeliverTelegramMessageService,
) {
    @PostMapping
    fun send(
        @RequestBody request: SendTelegramMessageRequest,
    ): TelegramDeliveryResponse =
        service
            .send(
                telegramId = request.telegramId,
                message = request.message.toDomain(),
            ).toResponse()

    @PutMapping
    fun edit(
        @RequestBody request: EditTelegramMessageRequest,
    ): TelegramDeliveryResponse =
        service
            .edit(
                telegramId = request.telegramId,
                messageId = request.messageId,
                message = request.message.toDomain(),
            ).toResponse()
}
