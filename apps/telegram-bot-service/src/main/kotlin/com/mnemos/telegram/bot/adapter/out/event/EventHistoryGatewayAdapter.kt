package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.telegram.bot.adapter.out.event.EventHistoryMapper.toModel
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
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
                        sort = InternalEventFeedSort.NEWEST,
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

    private companion object {
        const val PAGE_SIZE = 8
    }
}
