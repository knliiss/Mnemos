package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import org.springframework.stereotype.Component

@Component
class EventHistoryMenuScreenDataProvider(
    private val eventHistoryGateway: EventHistoryGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.EVENTS

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData {
        val filter = EventHistoryMenuContext.selectedFilter(session.screenContext)
        val search = EventHistoryMenuContext.searchQuery(session.screenContext)

        if (session.pendingInput == TelegramPendingInput.EVENT_SEARCH) {
            return TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.SearchPrompt(
                    filter = filter,
                    currentSearch = search,
                    error = EventHistoryMenuContext.searchError(session.screenContext),
                ),
            )
        }

        val query =
            EventHistoryQuery(
                filter = filter,
                after = EventHistoryMenuContext.after(session.screenContext),
                search = search,
            )

        return TelegramMenuScreenData.EventHistory(
            EventHistoryMenuScreenData.Feed(
                filter = filter,
                search = search,
                result = eventHistoryGateway.getPage(query),
                hasPrevious = EventHistoryMenuContext.hasPrevious(session.screenContext),
            ),
        )
    }
}
