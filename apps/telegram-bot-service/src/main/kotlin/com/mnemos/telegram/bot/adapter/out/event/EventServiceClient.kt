package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.event.contracts.internal.v1.InternalWipeListResponse
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
import java.util.UUID

@FeignClient(
    name = "telegram-bot-event-service-client",
    url = "\${mnemos.clients.event-service.url}",
    configuration = [EventServiceFeignConfiguration::class],
)
interface EventServiceClient {
    @GetMapping("/internal/v1/events/feed")
    fun listEvents(
        @RequestParam(required = false)
        after: UUID?,
        @RequestParam(required = false)
        type: InternalEventFeedType?,
        @RequestParam(required = false)
        search: String?,
        @RequestParam(required = false)
        itemRarity: ContractItemRarity?,
        @RequestParam(required = false)
        itemType: ContractItemType?,
        @RequestParam(required = false)
        boosterType: ContractBoosterType?,
        @RequestParam(required = false)
        globalType: ContractGlobalEventType?,
        @RequestParam(required = false)
        raidLocation: Int?,
        @RequestParam(required = false)
        wipeId: UUID?,
        @RequestParam
        sort: InternalEventFeedSort,
        @RequestParam
        limit: Int,
        @RequestParam(required = false)
        droppedFor: String? = null,
    ): InternalEventFeedResponse

    @GetMapping("/internal/v1/events/localizations/sao/{locale}")
    fun getSaoLocalization(
        @PathVariable locale: String,
    ): TelegramSaoLocalizationResponse

    @GetMapping("/internal/v1/wipes")
    fun listWipes(): InternalWipeListResponse
}

data class TelegramSaoLocalizationResponse(
    val locale: String,
    val hash: String,
    val properties: Map<String, String>,
    val updatedAt: Instant,
)
