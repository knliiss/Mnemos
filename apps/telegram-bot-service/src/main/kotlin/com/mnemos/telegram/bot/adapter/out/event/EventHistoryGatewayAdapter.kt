package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import com.mnemos.telegram.bot.adapter.out.event.EventHistoryMapper.toModel
import com.mnemos.telegram.bot.application.events.model.EventHistoryBoosterType
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryGlobalType
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.model.EventHistorySort
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventHistoryGatewayAdapter(
    private val client: EventServiceClient,
) : EventHistoryGateway {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getPage(query: EventHistoryQuery): EventHistoryResult {
        val result =
            try {
                val response =
                    client.listEvents(
                        after = query.after,
                        type = query.filter.toInternalType(),
                        search = query.search,
                        itemRarity = query.itemRarity?.toContract(),
                        itemType = query.itemType?.toContract(),
                        boosterType = query.boosterType?.toContract(),
                        globalType = query.globalType?.toContract(),
                        raidLocation = query.raidLocation,
                        wipeId = query.wipeId,
                        sort = query.sort.toInternalSort(),
                        limit = PAGE_SIZE,
                    )

                EventHistoryResult.Available(response.toModel())
            } catch (exception: RuntimeException) {
                logger.warn("Could not load event history from event-service", exception)
                EventHistoryResult.Unavailable
            }

        return result
    }

    private fun EventHistoryFilter.toInternalType(): InternalEventFeedType? =
        when (this) {
            EventHistoryFilter.ALL -> null
            EventHistoryFilter.ITEM_DROP -> InternalEventFeedType.ITEM_DROP
            EventHistoryFilter.BOOSTER -> InternalEventFeedType.BOOSTER
            EventHistoryFilter.GLOBAL -> InternalEventFeedType.GLOBAL
            EventHistoryFilter.RAID -> InternalEventFeedType.RAID
        }

    private fun EventHistorySort.toInternalSort(): InternalEventFeedSort =
        when (this) {
            EventHistorySort.NEWEST -> InternalEventFeedSort.NEWEST
            EventHistorySort.OLDEST -> InternalEventFeedSort.OLDEST
        }

    private fun EventHistoryItemRarity.toContract(): ContractItemRarity = ContractItemRarity.valueOf(name)

    private fun EventHistoryItemType.toContract(): ContractItemType = ContractItemType.valueOf(name)

    private fun EventHistoryBoosterType.toContract(): ContractBoosterType = ContractBoosterType.valueOf(name)

    private fun EventHistoryGlobalType.toContract(): ContractGlobalEventType = ContractGlobalEventType.valueOf(name)

    private companion object {
        const val PAGE_SIZE = 8
    }
}
