package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.adapter.inbound.http.internal.InternalEventFeedApiMapper.toInternalResponse
import com.mnemos.event.adapter.inbound.http.internal.InternalEventFeedApiMapper.toQuery
import com.mnemos.event.application.feed.usecase.list.ListEventFeedService
import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/internal/v1/events/feed")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalEventFeedController(
    private val listEventFeedService: ListEventFeedService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false)
        after: UUID?,
        @RequestParam(required = false)
        type: InternalEventFeedType?,
        @RequestParam(required = false)
        @Size(max = 64)
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
        @Min(1)
        @Max(28)
        raidLocation: Int?,
        @RequestParam(required = false)
        wipeId: UUID?,
        @RequestParam(defaultValue = "NEWEST")
        sort: InternalEventFeedSort,
        @RequestParam(defaultValue = "8")
        @Min(1)
        @Max(100)
        limit: Int,
        @RequestParam(required = false)
        @Size(max = 64)
        droppedFor: String? = null,
    ): InternalEventFeedResponse =
        listEventFeedService
            .execute(
                toQuery(
                    after = after,
                    type = type,
                    search = search,
                    droppedFor = droppedFor,
                    itemRarity = itemRarity,
                    itemType = itemType,
                    boosterType = boosterType,
                    globalType = globalType,
                    raidLocation = raidLocation,
                    wipeId = wipeId,
                    sort = sort,
                    limit = limit,
                ),
            ).toInternalResponse()
}
