package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryBoosterType
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryGlobalType
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistorySort
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeSelection
import java.util.UUID

object EventHistoryMenuContext {
    const val FILTER_KEY = "events.filter"
    const val SEARCH_KEY = "events.search"
    const val SEARCH_ERROR_KEY = "events.search-error"
    const val SORT_KEY = "events.sort"
    const val ITEM_RARITY_KEY = "events.item-rarity"
    const val ITEM_TYPE_KEY = "events.item-type"
    const val BOOSTER_TYPE_KEY = "events.booster-type"
    const val GLOBAL_TYPE_KEY = "events.global-type"
    const val RAID_LOCATION_KEY = "events.raid-location"
    const val RAID_LOCATION_PAGE_KEY = "events.raid-location-page"
    const val WIPE_SELECTION_KEY = "events.wipe-selection"
    const val AFTER_KEY = "events.after"
    const val CURSOR_STACK_KEY = "events.cursor-stack"
    const val FILTERS_MODE_KEY = "events.filters-mode"

    private const val ROOT_CURSOR = "root"
    private const val STACK_SEPARATOR = ","

    fun openFilters(context: Map<String, String>): Map<String, String> =
        context + (FILTERS_MODE_KEY to "1")

    fun closeFilters(context: Map<String, String>): Map<String, String> =
        context - FILTERS_MODE_KEY - RAID_LOCATION_PAGE_KEY

    fun filtersOpen(context: Map<String, String>): Boolean = context[FILTERS_MODE_KEY] == "1"

    fun filter(
        context: Map<String, String>,
        filter: EventHistoryFilter,
    ): Map<String, String> {
        if (selectedFilter(context) == filter) {
            return context
        }

        val resetContext = resetKindFilters(resetPagination(context))

        return if (filter == EventHistoryFilter.ALL) {
            resetContext - FILTER_KEY
        } else {
            resetContext + (FILTER_KEY to filter.name)
        }
    }

    fun selectedFilter(context: Map<String, String>): EventHistoryFilter =
        context[FILTER_KEY]
            ?.let { value -> runCatching { EventHistoryFilter.valueOf(value) }.getOrNull() }
            ?: EventHistoryFilter.ALL

    fun sort(
        context: Map<String, String>,
        sort: EventHistorySort,
    ): Map<String, String> {
        val resetContext = resetPagination(context)

        return if (sort == EventHistorySort.NEWEST) {
            resetContext - SORT_KEY
        } else {
            resetContext + (SORT_KEY to sort.name)
        }
    }

    fun selectedSort(context: Map<String, String>): EventHistorySort =
        context[SORT_KEY]
            ?.let { value -> runCatching { EventHistorySort.valueOf(value) }.getOrNull() }
            ?: EventHistorySort.NEWEST

    fun itemRarity(
        context: Map<String, String>,
        rarity: EventHistoryItemRarity?,
    ): Map<String, String> = setOptionalFilter(context, ITEM_RARITY_KEY, rarity?.name)

    fun selectedItemRarity(context: Map<String, String>): EventHistoryItemRarity? =
        context[ITEM_RARITY_KEY]
            ?.let { value -> runCatching { EventHistoryItemRarity.valueOf(value) }.getOrNull() }

    fun itemType(
        context: Map<String, String>,
        itemType: EventHistoryItemType?,
    ): Map<String, String> = setOptionalFilter(context, ITEM_TYPE_KEY, itemType?.name)

    fun selectedItemType(context: Map<String, String>): EventHistoryItemType? =
        context[ITEM_TYPE_KEY]
            ?.let { value -> runCatching { EventHistoryItemType.valueOf(value) }.getOrNull() }

    fun boosterType(
        context: Map<String, String>,
        boosterType: EventHistoryBoosterType?,
    ): Map<String, String> = setOptionalFilter(context, BOOSTER_TYPE_KEY, boosterType?.name)

    fun selectedBoosterType(context: Map<String, String>): EventHistoryBoosterType? =
        context[BOOSTER_TYPE_KEY]
            ?.let { value -> runCatching { EventHistoryBoosterType.valueOf(value) }.getOrNull() }

    fun globalType(
        context: Map<String, String>,
        globalType: EventHistoryGlobalType?,
    ): Map<String, String> = setOptionalFilter(context, GLOBAL_TYPE_KEY, globalType?.name)

    fun selectedGlobalType(context: Map<String, String>): EventHistoryGlobalType? =
        context[GLOBAL_TYPE_KEY]
            ?.let { value -> runCatching { EventHistoryGlobalType.valueOf(value) }.getOrNull() }

    fun raidLocation(
        context: Map<String, String>,
        location: Int?,
    ): Map<String, String> {
        require(location == null || location in 1..28) { "Raid location must be between 1 and 28" }

        return setOptionalFilter(context, RAID_LOCATION_KEY, location?.toString())
    }

    fun selectedRaidLocation(context: Map<String, String>): Int? =
        context[RAID_LOCATION_KEY]
            ?.toIntOrNull()
            ?.takeIf { location -> location in 1..28 }

    fun raidLocationPage(
        context: Map<String, String>,
        page: Int,
    ): Map<String, String> {
        require(page in 0..3) { "Raid location page must be between 0 and 3" }

        return if (page == 0) {
            context - RAID_LOCATION_PAGE_KEY
        } else {
            context + (RAID_LOCATION_PAGE_KEY to page.toString())
        }
    }

    fun selectedRaidLocationPage(context: Map<String, String>): Int =
        context[RAID_LOCATION_PAGE_KEY]
            ?.toIntOrNull()
            ?.coerceIn(0, 3)
            ?: 0

    fun wipe(
        context: Map<String, String>,
        selection: EventHistoryWipeSelection,
    ): Map<String, String> {
        val resetContext = resetPagination(context)

        return if (selection == EventHistoryWipeSelection.ALL) {
            resetContext - WIPE_SELECTION_KEY
        } else {
            resetContext + (WIPE_SELECTION_KEY to selection.name)
        }
    }

    fun selectedWipe(context: Map<String, String>): EventHistoryWipeSelection =
        context[WIPE_SELECTION_KEY]
            ?.let { value -> runCatching { EventHistoryWipeSelection.valueOf(value) }.getOrNull() }
            ?: EventHistoryWipeSelection.ALL

    fun search(
        context: Map<String, String>,
        query: String,
    ): Map<String, String> = closeFilters(resetPagination(context)) + (SEARCH_KEY to query)

    fun clearSearch(context: Map<String, String>): Map<String, String> = resetPagination(context) - SEARCH_KEY

    fun searchQuery(context: Map<String, String>): String? = context[SEARCH_KEY]

    fun searchError(context: Map<String, String>): String? = context[SEARCH_ERROR_KEY]

    fun clearSearchError(context: Map<String, String>): Map<String, String> = context - SEARCH_ERROR_KEY

    fun withSearchError(
        context: Map<String, String>,
        error: String,
    ): Map<String, String> = context + (SEARCH_ERROR_KEY to error)

    fun after(context: Map<String, String>): UUID? =
        context[AFTER_KEY]
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    fun hasPrevious(context: Map<String, String>): Boolean = cursorStack(context).isNotEmpty()

    fun next(
        context: Map<String, String>,
        cursor: UUID,
    ): Map<String, String> {
        val currentCursor = context[AFTER_KEY] ?: ROOT_CURSOR
        val stack = cursorStack(context) + currentCursor

        return context +
            mapOf(
                AFTER_KEY to cursor.toString(),
                CURSOR_STACK_KEY to stack.joinToString(STACK_SEPARATOR),
            )
    }

    fun previous(context: Map<String, String>): Map<String, String> {
        val stack = cursorStack(context)
        val previousCursor = stack.lastOrNull() ?: return context
        val remainingStack = stack.dropLast(1)
        var nextContext = context - AFTER_KEY - CURSOR_STACK_KEY

        if (previousCursor != ROOT_CURSOR) {
            nextContext += AFTER_KEY to previousCursor
        }

        if (remainingStack.isNotEmpty()) {
            nextContext += CURSOR_STACK_KEY to remainingStack.joinToString(STACK_SEPARATOR)
        }

        return nextContext
    }

    private fun setOptionalFilter(
        context: Map<String, String>,
        key: String,
        value: String?,
    ): Map<String, String> {
        val resetContext = resetPagination(context)

        return if (value == null) {
            resetContext - key
        } else {
            resetContext + (key to value)
        }
    }

    private fun resetKindFilters(context: Map<String, String>): Map<String, String> =
        context -
            ITEM_RARITY_KEY -
            ITEM_TYPE_KEY -
            BOOSTER_TYPE_KEY -
            GLOBAL_TYPE_KEY -
            RAID_LOCATION_KEY -
            RAID_LOCATION_PAGE_KEY

    private fun resetPagination(context: Map<String, String>): Map<String, String> =
        context - AFTER_KEY - CURSOR_STACK_KEY - SEARCH_ERROR_KEY

    private fun cursorStack(context: Map<String, String>): List<String> =
        context[CURSOR_STACK_KEY]
            ?.split(STACK_SEPARATOR)
            ?.filter(String::isNotBlank)
            .orEmpty()
}
