package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.events.EventHistoryCallbackData
import com.mnemos.telegram.bot.application.events.model.EventHistoryBoosterType
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryGlobalType
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.model.EventHistorySort
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

object EventHistoryMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val historyData =
            data as? TelegramMenuScreenData.EventHistory
                ?: return unavailableMessage()

        return when (val screenData = historyData.data) {
            is EventHistoryMenuScreenData.Feed -> renderFeed(screenData)
            is EventHistoryMenuScreenData.SearchPrompt -> renderSearchPrompt(screenData)
        }
    }

    private fun renderFeed(data: EventHistoryMenuScreenData.Feed): OutboundTelegramMessage =
        when (val result = data.result) {
            is EventHistoryResult.Available -> availableMessage(data, result)
            EventHistoryResult.Unavailable -> unavailableMessage(data)
        }

    private fun availableMessage(
        data: EventHistoryMenuScreenData.Feed,
        result: EventHistoryResult.Available,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = renderAvailableText(data, result),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderFeedActions(data, result.page.cursor),
        )

    private fun renderAvailableText(
        data: EventHistoryMenuScreenData.Feed,
        result: EventHistoryResult.Available,
    ): String {
        val eventLines =
            result.page.items
                .mapIndexed { index, item -> "${index + 1}. ${renderItem(item)}" }
                .joinToString("\n\n")
                .ifBlank {
                    "Событий по выбранным условиям пока нет."
                }
        val lines =
            mutableListOf(
                "🕘 <b>История событий</b>",
                "Фильтр: <b>${filterLabel(data.filter)}</b>",
                "Сортировка: <b>${sortLabel(data.sort)}</b>",
            )

        lines += detailedFilterLines(data)

        data.search?.let { search ->
            lines += "Поиск: <code>${escapeHtml(search)}</code>"
        }

        lines += ""
        lines += eventLines

        return lines.joinToString("\n")
    }

    private fun detailedFilterLines(data: EventHistoryMenuScreenData.Feed): List<String> =
        when (data.filter) {
            EventHistoryFilter.ITEM_DROP ->
                listOf(
                    "Редкость: <b>${itemRarityLabel(data.itemRarity)}</b>",
                    "Тип предмета: <b>${itemTypeLabel(data.itemType)}</b>",
                )

            EventHistoryFilter.BOOSTER ->
                listOf("Бустер: <b>${boosterTypeLabel(data.boosterType)}</b>")

            EventHistoryFilter.GLOBAL ->
                listOf("Событие: <b>${globalTypeLabel(data.globalType)}</b>")

            EventHistoryFilter.RAID ->
                listOf("Локация: <b>${data.raidLocation?.toString() ?: "Любая"}</b>")

            EventHistoryFilter.ALL -> emptyList()
        }

    private fun renderItem(item: EventHistoryItem): String {
        val occurredAt = eventTimestampFormatter.format(item.occurredAt)

        return when (item) {
            is EventHistoryItem.ItemDrop ->
                "⚔️ <b>${escapeHtml(item.itemName)}</b> → <code>${escapeHtml(item.droppedFor)}</code>\n" +
                    "${escapeHtml(item.itemRarity)} · ${escapeHtml(item.itemType)} · <code>$occurredAt</code>"

            is EventHistoryItem.Booster ->
                "⚡ <b>${escapeHtml(item.boosterType)}</b> · <code>${escapeHtml(item.activatedBy)}</code>\n" +
                    "<code>$occurredAt</code>"

            is EventHistoryItem.Global ->
                "🌒 <b>${escapeHtml(item.globalType)}</b>\n<code>$occurredAt</code>"

            is EventHistoryItem.Raid -> {
                val locations =
                    item.locations
                        .sorted()
                        .joinToString(", ")
                        .ifBlank { "—" }

                "🛡 <b>Рейд</b> · локации ${escapeHtml(locations)}\n<code>$occurredAt</code>"
            }
        }
    }

    private fun renderSearchPrompt(data: EventHistoryMenuScreenData.SearchPrompt): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🔎 <b>Поиск по событиям</b>",
                "Фильтр: <b>${filterLabel(data.filter)}</b>",
                "",
                "Отправьте название предмета, никнейм или часть запроса одним сообщением.",
                "От 1 до 64 символов.",
            )

        data.currentSearch?.let { currentSearch ->
            lines += ""
            lines += "Текущий поиск: <code>${escapeHtml(currentSearch)}</code>"
        }

        data.error?.let { error ->
            lines += ""
            lines += "⚠️ ${escapeHtml(error)}"
        }

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = searchPromptActions,
        )
    }

    private fun renderFeedActions(
        data: EventHistoryMenuScreenData.Feed,
        cursor: UUID?,
    ): List<OutboundTelegramMessageAction> {
        val actions = filterActions(data.filter).toMutableList()

        actions += detailedFilterActions(data)
        actions += sortActions(data.sort)
        actions +=
            OutboundTelegramMessageAction.Callback(
                "🔎 Поиск",
                EventHistoryCallbackData.SEARCH,
            )

        if (data.search != null) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    "✖ Сбросить поиск",
                    EventHistoryCallbackData.SEARCH_CLEAR,
                )
        }

        actions +=
            OutboundTelegramMessageAction.Callback(
                "🔄 Обновить",
                EventHistoryCallbackData.REFRESH,
            )

        if (data.hasPrevious) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    "← Страница",
                    EventHistoryCallbackData.PREVIOUS,
                )
        }

        cursor?.let { nextCursor ->
            actions +=
                OutboundTelegramMessageAction.Callback(
                    "Дальше →",
                    EventHistoryCallbackData.next(nextCursor),
                )
        }

        actions += commonNavigationActions

        return actions
    }

    private fun detailedFilterActions(data: EventHistoryMenuScreenData.Feed): List<OutboundTelegramMessageAction> =
        when (data.filter) {
            EventHistoryFilter.ITEM_DROP -> itemDropFilterActions(data)
            EventHistoryFilter.BOOSTER -> boosterFilterActions(data.boosterType)
            EventHistoryFilter.GLOBAL -> globalFilterActions(data.globalType)
            EventHistoryFilter.RAID -> raidFilterActions(data.raidLocation, data.raidLocationPage)
            EventHistoryFilter.ALL -> emptyList()
        }

    private fun itemDropFilterActions(data: EventHistoryMenuScreenData.Feed): List<OutboundTelegramMessageAction> =
        itemRarityActions(data.itemRarity) + itemTypeActions(data.itemType)

    private fun itemRarityActions(selected: EventHistoryItemRarity?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            filterCallback(
                label = selectionLabel("Любая редкость", selected == null),
                data = EventHistoryCallbackData.itemRarity(null),
            ),
        ) +
            EventHistoryItemRarity.entries.map { rarity ->
                filterCallback(
                    label = selectionLabel(itemRarityLabel(rarity), rarity == selected),
                    data = EventHistoryCallbackData.itemRarity(rarity),
                )
            }

    private fun itemTypeActions(selected: EventHistoryItemType?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            filterCallback(
                label = selectionLabel("Любой тип", selected == null),
                data = EventHistoryCallbackData.itemType(null),
            ),
        ) +
            EventHistoryItemType.entries.map { itemType ->
                filterCallback(
                    label = selectionLabel(itemTypeLabel(itemType), itemType == selected),
                    data = EventHistoryCallbackData.itemType(itemType),
                )
            }

    private fun boosterFilterActions(selected: EventHistoryBoosterType?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            filterCallback(
                label = selectionLabel("Любой бустер", selected == null),
                data = EventHistoryCallbackData.boosterType(null),
            ),
        ) +
            EventHistoryBoosterType.entries.map { boosterType ->
                filterCallback(
                    label = selectionLabel(boosterTypeLabel(boosterType), boosterType == selected),
                    data = EventHistoryCallbackData.boosterType(boosterType),
                )
            }

    private fun globalFilterActions(selected: EventHistoryGlobalType?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            filterCallback(
                label = selectionLabel("Любое событие", selected == null),
                data = EventHistoryCallbackData.globalType(null),
            ),
        ) +
            EventHistoryGlobalType.entries.map { globalType ->
                filterCallback(
                    label = selectionLabel(globalTypeLabel(globalType), globalType == selected),
                    data = EventHistoryCallbackData.globalType(globalType),
                )
            }

    private fun raidFilterActions(
        selected: Int?,
        page: Int,
    ): List<OutboundTelegramMessageAction> {
        val firstLocation = page * RAID_LOCATIONS_PER_PAGE + 1
        val lastLocation = minOf(firstLocation + RAID_LOCATIONS_PER_PAGE - 1, MAX_RAID_LOCATION)
        val actions =
            mutableListOf<OutboundTelegramMessageAction>(
                filterCallback(
                    label = selectionLabel("Любая локация", selected == null),
                    data = EventHistoryCallbackData.raidLocation(null),
                ),
            )

        (firstLocation..lastLocation).forEach { location ->
            actions +=
                filterCallback(
                    label = selectionLabel("Локация $location", location == selected),
                    data = EventHistoryCallbackData.raidLocation(location),
                )
        }

        if (page > 0) {
            actions +=
                filterCallback(
                    label = "← Локации",
                    data = EventHistoryCallbackData.raidLocationPage(page - 1),
                )
        }

        if (page < LAST_RAID_LOCATION_PAGE) {
            actions +=
                filterCallback(
                    label = "Локации →",
                    data = EventHistoryCallbackData.raidLocationPage(page + 1),
                )
        }

        return actions
    }

    private fun filterActions(selected: EventHistoryFilter): List<OutboundTelegramMessageAction> =
        EventHistoryFilter.entries.map { filter ->
            filterCallback(
                label = selectionLabel(filterLabel(filter), filter == selected),
                data = EventHistoryCallbackData.filter(filter),
            )
        }

    private fun sortActions(selected: EventHistorySort): List<OutboundTelegramMessageAction> =
        EventHistorySort.entries.map { sort ->
            filterCallback(
                label = selectionLabel(sortLabel(sort), sort == selected),
                data = EventHistoryCallbackData.sort(sort),
            )
        }

    private fun filterCallback(
        label: String,
        data: String,
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
        )

    private fun selectionLabel(
        label: String,
        selected: Boolean,
    ): String = if (selected) "✓ $label" else label

    private fun filterLabel(filter: EventHistoryFilter): String =
        when (filter) {
            EventHistoryFilter.ALL -> "Все"
            EventHistoryFilter.ITEM_DROP -> "Дропы"
            EventHistoryFilter.BOOSTER -> "Бустеры"
            EventHistoryFilter.GLOBAL -> "Глобальные"
            EventHistoryFilter.RAID -> "Рейды"
        }

    private fun sortLabel(sort: EventHistorySort): String =
        when (sort) {
            EventHistorySort.NEWEST -> "Сначала новые"
            EventHistorySort.OLDEST -> "Сначала старые"
        }

    private fun itemRarityLabel(rarity: EventHistoryItemRarity?): String =
        when (rarity) {
            EventHistoryItemRarity.MYTHICAL -> "Mythical"
            EventHistoryItemRarity.SECRET -> "Secret"
            null -> "Любая"
        }

    private fun itemTypeLabel(itemType: EventHistoryItemType?): String =
        when (itemType) {
            EventHistoryItemType.SWORD -> "Мечи"
            EventHistoryItemType.AURA -> "Ауры"
            EventHistoryItemType.PET -> "Петы"
            EventHistoryItemType.BOOK -> "Книги"
            EventHistoryItemType.JEWELRY -> "Бижутерия"
            null -> "Любой"
        }

    private fun boosterTypeLabel(boosterType: EventHistoryBoosterType?): String =
        when (boosterType) {
            EventHistoryBoosterType.LUCK -> "Luck"
            EventHistoryBoosterType.MONEY -> "Money"
            EventHistoryBoosterType.DAMAGE -> "Damage"
            EventHistoryBoosterType.POWER -> "Power"
            null -> "Любой"
        }

    private fun globalTypeLabel(globalType: EventHistoryGlobalType?): String =
        when (globalType) {
            EventHistoryGlobalType.DARKNESS -> "Darkness"
            EventHistoryGlobalType.MOON -> "Moon"
            EventHistoryGlobalType.ECLIPSE -> "Eclipse"
            EventHistoryGlobalType.EXPLOSION -> "Explosion"
            EventHistoryGlobalType.COMET_CHAOS -> "Comet Chaos"
            null -> "Любое"
        }

    private fun unavailableMessage(data: EventHistoryMenuScreenData.Feed? = null): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "🕘 <b>История событий</b>",
                    "",
                    "Не удалось получить историю событий. Попробуйте обновить через несколько секунд.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions =
                data?.let { feed -> renderFeedActions(feed, null) }
                    ?: genericUnavailableActions(),
        )

    private fun genericUnavailableActions(): List<OutboundTelegramMessageAction> =
        listOf(
            OutboundTelegramMessageAction.Callback(
                "🔄 Обновить",
                EventHistoryCallbackData.REFRESH,
            ),
        ) + commonNavigationActions

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private val searchPromptActions =
        listOf(
            OutboundTelegramMessageAction.Callback(
                "✖ Отмена",
                EventHistoryCallbackData.SEARCH_CANCEL,
            ),
            OutboundTelegramMessageAction.Callback(
                "⌂ Меню",
                TelegramMenuScreen.HOME.callbackData,
            ),
            OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
        )

    private val commonNavigationActions =
        listOf(
            OutboundTelegramMessageAction.Callback(
                "← Назад",
                TelegramMenuScreen.BACK_CALLBACK_DATA,
            ),
            OutboundTelegramMessageAction.Callback(
                "⌂ Меню",
                TelegramMenuScreen.HOME.callbackData,
            ),
            OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
        )

    private val eventTimestampFormatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("dd.MM HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC)

    private const val RAID_LOCATIONS_PER_PAGE = 7
    private const val MAX_RAID_LOCATION = 28
    private const val LAST_RAID_LOCATION_PAGE = 3
}
