package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryBoosterType
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryGlobalType
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistorySort
import java.util.UUID

object EventHistoryCallbackData {
    const val PREFIX = "ev:"
    const val REFRESH = "ev:r"
    const val PREVIOUS = "ev:p"
    const val SEARCH = "ev:s"
    const val SEARCH_CANCEL = "ev:s:c"
    const val SEARCH_CLEAR = "ev:s:x"

    private const val NEXT_PREFIX = "ev:n:"
    private const val FILTER_PREFIX = "ev:f:"
    private const val SORT_PREFIX = "ev:o:"
    private const val ITEM_RARITY_PREFIX = "ev:ir:"
    private const val ITEM_TYPE_PREFIX = "ev:it:"
    private const val BOOSTER_TYPE_PREFIX = "ev:b:"
    private const val GLOBAL_TYPE_PREFIX = "ev:g:"
    private const val RAID_LOCATION_PREFIX = "ev:l:"
    private const val RAID_LOCATION_PAGE_PREFIX = "ev:lp:"
    private const val CLEAR_VALUE = "x"

    fun next(cursor: UUID): String = "$NEXT_PREFIX$cursor"

    fun nextCursor(callbackData: String): UUID? =
        callbackData
            .takeIf { value -> value.startsWith(NEXT_PREFIX) }
            ?.removePrefix(NEXT_PREFIX)
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    fun filter(filter: EventHistoryFilter): String = "$FILTER_PREFIX${filter.name}"

    fun selectedFilter(callbackData: String): EventHistoryFilter? =
        enumValue(callbackData, FILTER_PREFIX, EventHistoryFilter::valueOf)

    fun sort(sort: EventHistorySort): String = "$SORT_PREFIX${sort.name}"

    fun selectedSort(callbackData: String): EventHistorySort? =
        enumValue(callbackData, SORT_PREFIX, EventHistorySort::valueOf)

    fun itemRarity(rarity: EventHistoryItemRarity?): String =
        "$ITEM_RARITY_PREFIX${rarity?.name ?: CLEAR_VALUE}"

    fun selectedItemRarity(callbackData: String): FilterCallbackValue<EventHistoryItemRarity>? =
        optionalEnumValue(callbackData, ITEM_RARITY_PREFIX, EventHistoryItemRarity::valueOf)

    fun itemType(itemType: EventHistoryItemType?): String =
        "$ITEM_TYPE_PREFIX${itemType?.name ?: CLEAR_VALUE}"

    fun selectedItemType(callbackData: String): FilterCallbackValue<EventHistoryItemType>? =
        optionalEnumValue(callbackData, ITEM_TYPE_PREFIX, EventHistoryItemType::valueOf)

    fun boosterType(boosterType: EventHistoryBoosterType?): String =
        "$BOOSTER_TYPE_PREFIX${boosterType?.name ?: CLEAR_VALUE}"

    fun selectedBoosterType(callbackData: String): FilterCallbackValue<EventHistoryBoosterType>? =
        optionalEnumValue(callbackData, BOOSTER_TYPE_PREFIX, EventHistoryBoosterType::valueOf)

    fun globalType(globalType: EventHistoryGlobalType?): String =
        "$GLOBAL_TYPE_PREFIX${globalType?.name ?: CLEAR_VALUE}"

    fun selectedGlobalType(callbackData: String): FilterCallbackValue<EventHistoryGlobalType>? =
        optionalEnumValue(callbackData, GLOBAL_TYPE_PREFIX, EventHistoryGlobalType::valueOf)

    fun raidLocation(location: Int?): String = "$RAID_LOCATION_PREFIX${location ?: CLEAR_VALUE}"

    fun selectedRaidLocation(callbackData: String): FilterCallbackValue<Int>? {
        val rawValue = callbackValue(callbackData, RAID_LOCATION_PREFIX) ?: return null

        if (rawValue == CLEAR_VALUE) {
            return FilterCallbackValue.Clear
        }

        val location = rawValue.toIntOrNull()?.takeIf { value -> value in 1..28 } ?: return null
        return FilterCallbackValue.Selected(location)
    }

    fun raidLocationPage(page: Int): String = "$RAID_LOCATION_PAGE_PREFIX$page"

    fun selectedRaidLocationPage(callbackData: String): Int? =
        callbackValue(callbackData, RAID_LOCATION_PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page in 0..3 }

    private fun <T> enumValue(
        callbackData: String,
        prefix: String,
        parser: (String) -> T,
    ): T? =
        callbackValue(callbackData, prefix)
            ?.let { value -> runCatching { parser(value) }.getOrNull() }

    private fun <T> optionalEnumValue(
        callbackData: String,
        prefix: String,
        parser: (String) -> T,
    ): FilterCallbackValue<T>? {
        val rawValue = callbackValue(callbackData, prefix) ?: return null

        if (rawValue == CLEAR_VALUE) {
            return FilterCallbackValue.Clear
        }

        return runCatching { parser(rawValue) }
            .getOrNull()
            ?.let(FilterCallbackValue<T>::Selected)
    }

    private fun callbackValue(
        callbackData: String,
        prefix: String,
    ): String? =
        callbackData
            .takeIf { value -> value.startsWith(prefix) }
            ?.removePrefix(prefix)
            ?.takeIf(String::isNotEmpty)
}

sealed interface FilterCallbackValue<out T> {
    data class Selected<T>(
        val value: T,
    ) : FilterCallbackValue<T>

    data object Clear : FilterCallbackValue<Nothing>
}
