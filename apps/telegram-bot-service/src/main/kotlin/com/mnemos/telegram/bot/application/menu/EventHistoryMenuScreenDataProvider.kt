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
        val context = session.screenContext
        val filter = EventHistoryMenuContext.selectedFilter(context)
        val search = EventHistoryMenuContext.searchQuery(context)
        val sort = EventHistoryMenuContext.selectedSort(context)

        if (session.pendingInput == TelegramPendingInput.EVENT_SEARCH) {
            return TelegramMenuScreenData.EventHistory(
                EventHistoryMenuScreenData.SearchPrompt(
                    filter = filter,
                    currentSearch = search,
                    error = EventHistoryMenuContext.searchError(context),
                ),
            )
        }

        val itemRarity = EventHistoryMenuContext.selectedItemRarity(context)
        val itemType = EventHistoryMenuContext.selectedItemType(context)
        val boosterType = EventHistoryMenuContext.selectedBoosterType(context)
        val globalType = EventHistoryMenuContext.selectedGlobalType(context)
        val raidLocation = EventHistoryMenuContext.selectedRaidLocation(context)
        val wipeId = EventHistoryMenuContext.selectedWipeId(context)
        val query =
            EventHistoryQuery(
                filter = filter,
                after = EventHistoryMenuContext.after(context),
                search = search,
                sort = sort,
                itemRarity = itemRarity,
                itemType = itemType,
                boosterType = boosterType,
                globalType = globalType,
                raidLocation = raidLocation,
                wipeId = wipeId,
            )

        return TelegramMenuScreenData.EventHistory(
            EventHistoryMenuScreenData.Feed(
                filter = filter,
                search = search,
                sort = sort,
                itemRarity = itemRarity,
                itemType = itemType,
                boosterType = boosterType,
                globalType = globalType,
                raidLocation = raidLocation,
                wipeId = wipeId,
                result = eventHistoryGateway.getPage(query),
                hasPrevious = EventHistoryMenuContext.hasPrevious(context),
            ),
        )
    }
}
