package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeMetadataResult
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOptions
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeSelection
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.events.port.out.EventWipeMetadataGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import org.springframework.stereotype.Component

@Component
class EventHistoryMenuScreenDataProvider(
    private val eventHistoryGateway: EventHistoryGateway,
    private val eventWipeMetadataGateway: EventWipeMetadataGateway,
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
        val requestedWipeSelection = EventHistoryMenuContext.selectedWipe(context)
        val wipeOptions = loadWipeOptions()
        val resolvedWipe = wipeOptions.resolve(requestedWipeSelection)
        val wipeSelection = effectiveWipeSelection(requestedWipeSelection, resolvedWipe != null)
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
                wipeId = resolvedWipe?.id,
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
                raidLocationPage = EventHistoryMenuContext.selectedRaidLocationPage(context),
                wipeSelection = wipeSelection,
                wipeOptions = wipeOptions,
                result = eventHistoryGateway.getPage(query),
                hasPrevious = EventHistoryMenuContext.hasPrevious(context),
            ),
        )
    }

    private fun loadWipeOptions(): EventHistoryWipeOptions =
        when (val result = eventWipeMetadataGateway.getOptions()) {
            is EventHistoryWipeMetadataResult.Available -> result.options
            EventHistoryWipeMetadataResult.Unavailable -> EventHistoryWipeOptions(current = null, previous = null)
        }

    private fun effectiveWipeSelection(
        requested: EventHistoryWipeSelection,
        resolved: Boolean,
    ): EventHistoryWipeSelection =
        if (requested == EventHistoryWipeSelection.ALL || resolved) {
            requested
        } else {
            EventHistoryWipeSelection.ALL
        }
}
