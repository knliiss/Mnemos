package com.mnemos.notification.adapter.inbound.http.internal

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.adapter.inbound.http.internal.TelegramSubscriptionInternalApiMapper.toCreateResponse
import com.mnemos.notification.adapter.inbound.http.internal.TelegramSubscriptionInternalApiMapper.toDomainBoosterType
import com.mnemos.notification.adapter.inbound.http.internal.TelegramSubscriptionInternalApiMapper.toDomainDungeonType
import com.mnemos.notification.adapter.inbound.http.internal.TelegramSubscriptionInternalApiMapper.toDomainGlobalType
import com.mnemos.notification.adapter.inbound.http.internal.TelegramSubscriptionInternalApiMapper.toResponse
import com.mnemos.notification.application.common.pagination.PageQuery
import com.mnemos.notification.application.subscription.TelegramSubscriptionCreationService
import com.mnemos.notification.application.subscription.TelegramSubscriptionService
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateBoosterSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateDungeonSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateGlobalSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateItemDropSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateRaidSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateSubscriptionResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
    private val creationService: TelegramSubscriptionCreationService,
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

    @PostMapping("/{telegramId}/item-drop")
    fun createItemDrop(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateItemDropSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse =
        creationService
            .createItemDrop(
                telegramId = TelegramId(telegramId),
                droppedFor = request.droppedFor,
                itemName = request.itemName,
            ).toCreateResponse()

    @PostMapping("/{telegramId}/booster")
    fun createBooster(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateBoosterSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse =
        creationService
            .createBooster(
                telegramId = TelegramId(telegramId),
                boosterType = request.boosterType.toDomainBoosterType(),
            ).toCreateResponse()

    @PostMapping("/{telegramId}/dungeon")
    fun createDungeon(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateDungeonSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse =
        creationService
            .createDungeon(
                telegramId = TelegramId(telegramId),
                dungeonType = request.dungeonType.toDomainDungeonType(),
            ).toCreateResponse()

    @PostMapping("/{telegramId}/raid")
    fun createRaid(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateRaidSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse =
        creationService
            .createRaid(
                telegramId = TelegramId(telegramId),
                locations = request.locations,
            ).toCreateResponse()

    @PatchMapping("/{telegramId}/raid")
    fun updateRaid(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateRaidSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse =
        creationService
            .updateRaid(
                telegramId = TelegramId(telegramId),
                locations = request.locations,
            ).toCreateResponse()

    @PostMapping("/{telegramId}/global")
    fun createGlobal(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateGlobalSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse =
        creationService
            .createGlobal(
                telegramId = TelegramId(telegramId),
                eventType = request.eventType.toDomainGlobalType(),
            ).toCreateResponse()

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
