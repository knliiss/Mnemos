package com.mnemos.notification.adapter.inbound.http.internal

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.adapter.inbound.http.internal.TelegramSubscriptionInternalApiMapper.toResponse
import com.mnemos.notification.application.common.pagination.PageQuery
import com.mnemos.notification.application.subscription.TelegramSubscriptionService
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Validated
@RequestMapping("/internal/v1/telegram/subscriptions")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramSubscriptionInternalController(
    private val service: TelegramSubscriptionService,
) {
    @GetMapping("/{telegramId}")
    fun list(
        @PathVariable telegramId: Long,
        @RequestParam(defaultValue = "0")
        @Min(0)
        page: Int,
        @RequestParam(defaultValue = "8")
        @Min(1)
        @Max(20)
        size: Int,
    ): TelegramSubscriptionListResponse =
        service
            .list(
                telegramId = TelegramId(telegramId),
                page = PageQuery(page = page, size = size),
            ).toResponse()

    @DeleteMapping("/{telegramId}/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable telegramId: Long,
        @PathVariable subscriptionId: UUID,
    ) {
        service.delete(
            telegramId = TelegramId(telegramId),
            subscriptionId = SubscriptionId(subscriptionId),
        )
    }
}
