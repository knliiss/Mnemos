package com.mnemos.event.adapter.inbound.http.feed

import com.mnemos.event.application.feed.model.EventFeedKind
import com.mnemos.event.application.feed.model.EventFeedSort
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

data class ListEventFeedRequest(
    val after: UUID? = null,
    val type: Set<EventFeedKind> = emptySet(),
    @field:Size(max = 64)
    val search: String? = null,
    @field:Size(min = 4, max = 20)
    @field:Pattern(regexp = "[\\p{L}0-9_]+")
    val droppedFor: String? = null,
    val itemRarity: DomainItemRarity? = null,
    val itemType: DomainItemType? = null,
    val boosterType: DomainBoosterType? = null,
    val globalType: DomainGlobalEventType? = null,
    @field:Min(1)
    @field:Max(28)
    val raidLocation: Int? = null,
    val wipeId: UUID? = null,
    val sort: EventFeedSort = EventFeedSort.NEWEST,
    @field:Min(1)
    @field:Max(100)
    val limit: Int = 50,
)
