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
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeOption
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeSelection
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
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
    ): OutboundTelegramMessage {
        val rows = renderFeedRows(data, result.page.cursor)

        return OutboundTelegramMessage(
            text = renderAvailableText(data, result),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

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
                "📜 <b>История событий</b>",
                MESSAGE_DIVIDER,
                "",
                "🔎 <b>Фильтры</b>",
                "├ Тип: <b>${filterLabel(data.filter)}</b>",
                "├ Вайп: <b>${wipeLabel(data)}</b>",
                "└ Сортировка: <b>${sortLabel(data.sort)}</b>",
            )

        lines += detailedFilterLines(data)

        data.search?.let { search ->
            lines += "   Поиск: <code>${escapeHtml(search)}</code>"
        }

        lines += ""
        lines += eventLines

        return lines.joinToString("\n")
    }

    private fun detailedFilterLines(data: EventHistoryMenuScreenData.Feed): List<String> =
        when (data.filter) {
            EventHistoryFilter.ITEM_DROP ->
                listOf(
                    "   Редкость: <b>${itemRarityLabel(data.itemRarity)}</b>",
                    "   Предмет: <b>${itemTypeLabel(data.itemType)}</b>",
                )

            EventHistoryFilter.BOOSTER ->
                listOf("   Бустер: <b>${boosterTypeLabel(data.boosterType)}</b>")

            EventHistoryFilter.GLOBAL ->
                listOf("   Событие: <b>${globalTypeLabel(data.globalType)}</b>")

            EventHistoryFilter.RAID ->
                listOf("   Локация: <b>${data.raidLocation?.toString() ?: "Любая"}</b>")

            EventHistoryFilter.ALL -> emptyList()
        }

    private fun renderItem(item: EventHistoryItem): String {
        val occurredAt = eventTimestampFormatter.format(item.occurredAt)

        return when (item) {
            is EventHistoryItem.ItemDrop ->
                "📦 <b>${escapeHtml(item.itemName)}</b> → <code>${escapeHtml(item.droppedFor)}</code>\n" +
                    "└ ${escapeHtml(item.itemRarity)} · ${escapeHtml(item.itemType)} · <code>$occurredAt</code>"

            is EventHistoryItem.Booster ->
                "⚡ <b>${escapeHtml(item.boosterType)}</b> · <code>${escapeHtml(item.activatedBy)}</code>\n" +
                    "└ <code>$occurredAt</code>"

            is EventHistoryItem.Global ->
                "🌌 <b>${escapeHtml(item.globalType)}</b>\n└ <code>$occurredAt</code>"

            is EventHistoryItem.Raid -> {
                val locations =
                    item.locations
                        .sorted()
                        .joinToString(", ")
                        .ifBlank { "—" }

                "🛡 <b>Рейд</b> · ${escapeHtml(locations)}\n└ <code>$occurredAt</code>"
            }
        }
    }

    private fun renderSearchPrompt(data: EventHistoryMenuScreenData.SearchPrompt): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🔎 <b>Поиск по событиям</b>",
                MESSAGE_DIVIDER,
                "",
                "Фильтр: <b>${filterLabel(data.filter)}</b>",
                "",
                "Отправьте название предмета, никнейм или часть запроса.",
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

        return message(
            text = lines.joinToString("\n"),
            rows =
                listOf(
                    listOf(
                        callback(
                            label = "✖ Отмена",
                            data = EventHistoryCallbackData.SEARCH_CANCEL,
                            style = OutboundTelegramButtonStyle.DANGER,
                        ),
                    ),
                ),
        )
    }

    private fun renderFeedRows(
        data: EventHistoryMenuScreenData.Feed,
        cursor: UUID?,
    ): List<List<OutboundTelegramMessageAction>> =
        buildList {
            addAll(filterActions(data.filter).chunked(3))
            addAll(detailedFilterRows(data))
            add(wipeActions(data))
            add(sortActions(data.sort))

            val searchRow =
                mutableListOf<OutboundTelegramMessageAction>(
                    callback(
                        label = "🔎 Поиск",
                        data = EventHistoryCallbackData.SEARCH,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                )

            if (data.search != null) {
                searchRow +=
                    callback(
                        label = "✖ Сбросить",
                        data = EventHistoryCallbackData.SEARCH_CLEAR,
                        style = OutboundTelegramButtonStyle.DANGER,
                    )
            }

            add(searchRow)

            val pageRow = mutableListOf<OutboundTelegramMessageAction>()

            if (data.hasPrevious) {
                pageRow += callback("◀ Назад", EventHistoryCallbackData.PREVIOUS)
            }

            cursor?.let { nextCursor ->
                pageRow += callback("Дальше ▶", EventHistoryCallbackData.next(nextCursor))
            }

            if (pageRow.isNotEmpty()) {
                add(pageRow)
            }

            add(listOf(refreshAction))
            add(listOf(backAction))
        }

    private fun detailedFilterRows(data: EventHistoryMenuScreenData.Feed): List<List<OutboundTelegramMessageAction>> =
        when (data.filter) {
            EventHistoryFilter.ITEM_DROP ->
                itemRarityActions(data.itemRarity).chunked(3) +
                    itemTypeActions(data.itemType).chunked(3)

            EventHistoryFilter.BOOSTER -> boosterFilterActions(data.boosterType).chunked(3)
            EventHistoryFilter.GLOBAL -> globalFilterActions(data.globalType).chunked(3)
            EventHistoryFilter.RAID -> raidFilterRows(data.raidLocation, data.raidLocationPage)
            EventHistoryFilter.ALL -> emptyList()
        }

    private fun itemRarityActions(selected: EventHistoryItemRarity?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            selectionCallback(
                label = "Любая",
                selected = selected == null,
                data = EventHistoryCallbackData.itemRarity(null),
            ),
        ) +
            EventHistoryItemRarity.entries.map { rarity ->
                selectionCallback(
                    label = itemRarityLabel(rarity),
                    selected = rarity == selected,
                    data = EventHistoryCallbackData.itemRarity(rarity),
                )
            }

    private fun itemTypeActions(selected: EventHistoryItemType?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            selectionCallback(
                label = "Любой",
                selected = selected == null,
                data = EventHistoryCallbackData.itemType(null),
            ),
        ) +
            EventHistoryItemType.entries.map { itemType ->
                selectionCallback(
                    label = itemTypeLabel(itemType),
                    selected = itemType == selected,
                    data = EventHistoryCallbackData.itemType(itemType),
                )
            }

    private fun boosterFilterActions(selected: EventHistoryBoosterType?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            selectionCallback(
                label = "Любой",
                selected = selected == null,
                data = EventHistoryCallbackData.boosterType(null),
            ),
        ) +
            EventHistoryBoosterType.entries.map { boosterType ->
                selectionCallback(
                    label = boosterTypeLabel(boosterType),
                    selected = boosterType == selected,
                    data = EventHistoryCallbackData.boosterType(boosterType),
                )
            }

    private fun globalFilterActions(selected: EventHistoryGlobalType?): List<OutboundTelegramMessageAction> =
        listOf<OutboundTelegramMessageAction>(
            selectionCallback(
                label = "Любое",
                selected = selected == null,
                data = EventHistoryCallbackData.globalType(null),
            ),
        ) +
            EventHistoryGlobalType.entries.map { globalType ->
                selectionCallback(
                    label = globalTypeLabel(globalType),
                    selected = globalType == selected,
                    data = EventHistoryCallbackData.globalType(globalType),
                )
            }

    private fun raidFilterRows(
        selected: Int?,
        page: Int,
    ): List<List<OutboundTelegramMessageAction>> {
        val firstLocation = page * RAID_LOCATIONS_PER_PAGE + 1
        val lastLocation = minOf(firstLocation + RAID_LOCATIONS_PER_PAGE - 1, MAX_RAID_LOCATION)
        val locationRow =
            (firstLocation..lastLocation).map { location ->
                selectionCallback(
                    label = location.toString(),
                    selected = location == selected,
                    data = EventHistoryCallbackData.raidLocation(location),
                )
            }
        val pageRow = mutableListOf<OutboundTelegramMessageAction>()

        if (page > 0) {
            pageRow += callback("◀ Локации", EventHistoryCallbackData.raidLocationPage(page - 1))
        }

        if (page < LAST_RAID_LOCATION_PAGE) {
            pageRow += callback("Локации ▶", EventHistoryCallbackData.raidLocationPage(page + 1))
        }

        return buildList {
            add(
                listOf(
                    selectionCallback(
                        label = "◎ Любая локация",
                        selected = selected == null,
                        data = EventHistoryCallbackData.raidLocation(null),
                    ),
                ),
            )
            add(locationRow)

            if (pageRow.isNotEmpty()) {
                add(pageRow)
            }
        }
    }

    private fun wipeActions(data: EventHistoryMenuScreenData.Feed): List<OutboundTelegramMessageAction> =
        buildList {
            add(
                selectionCallback(
                    label = "Любой вайп",
                    selected = data.wipeSelection == EventHistoryWipeSelection.ALL,
                    data = EventHistoryCallbackData.wipe(EventHistoryWipeSelection.ALL),
                ),
            )

            data.wipeOptions.current?.let { wipe ->
                add(
                    selectionCallback(
                        label = wipeButtonLabel("Текущий", wipe),
                        selected = data.wipeSelection == EventHistoryWipeSelection.CURRENT,
                        data = EventHistoryCallbackData.wipe(EventHistoryWipeSelection.CURRENT),
                    ),
                )
            }

            data.wipeOptions.previous?.let { wipe ->
                add(
                    selectionCallback(
                        label = wipeButtonLabel("Предыдущий", wipe),
                        selected = data.wipeSelection == EventHistoryWipeSelection.PREVIOUS,
                        data = EventHistoryCallbackData.wipe(EventHistoryWipeSelection.PREVIOUS),
                    ),
                )
            }
        }

    private fun filterActions(selected: EventHistoryFilter): List<OutboundTelegramMessageAction> =
        EventHistoryFilter.entries.map { filter ->
            selectionCallback(
                label = filterLabel(filter),
                selected = filter == selected,
                data = EventHistoryCallbackData.filter(filter),
            )
        }

    private fun sortActions(selected: EventHistorySort): List<OutboundTelegramMessageAction> =
        EventHistorySort.entries.map { sort ->
            selectionCallback(
                label = sortLabel(sort),
                selected = sort == selected,
                data = EventHistoryCallbackData.sort(sort),
            )
        }

    private fun selectionCallback(
        label: String,
        selected: Boolean,
        data: String,
    ): OutboundTelegramMessageAction.Callback =
        callback(
            label = label,
            data = data,
            style =
                if (selected) {
                    OutboundTelegramButtonStyle.SUCCESS
                } else {
                    OutboundTelegramButtonStyle.DEFAULT
                },
        )

    private fun filterLabel(filter: EventHistoryFilter): String =
        when (filter) {
            EventHistoryFilter.ALL -> "Все"
            EventHistoryFilter.ITEM_DROP -> "📦 Дропы"
            EventHistoryFilter.BOOSTER -> "⚡ Бустеры"
            EventHistoryFilter.GLOBAL -> "🌌 Глобальные"
            EventHistoryFilter.RAID -> "🛡 Рейды"
        }

    private fun sortLabel(sort: EventHistorySort): String =
        when (sort) {
            EventHistorySort.NEWEST -> "Новые"
            EventHistorySort.OLDEST -> "Старые"
        }

    private fun wipeLabel(data: EventHistoryMenuScreenData.Feed): String =
        when (data.wipeSelection) {
            EventHistoryWipeSelection.ALL -> "Любой"
            EventHistoryWipeSelection.CURRENT -> wipeTextLabel("Текущий", data.wipeOptions.current)
            EventHistoryWipeSelection.PREVIOUS -> wipeTextLabel("Предыдущий", data.wipeOptions.previous)
        }

    private fun wipeTextLabel(
        prefix: String,
        wipe: EventHistoryWipeOption?,
    ): String {
        if (wipe == null) {
            return "Любой"
        }

        val name =
            wipe.name
                ?.takeIf(String::isNotBlank)
                ?.let { value -> " · ${escapeHtml(value)}" }
                .orEmpty()

        return "$prefix #${wipe.number}$name"
    }

    private fun wipeButtonLabel(
        prefix: String,
        wipe: EventHistoryWipeOption,
    ): String = "$prefix #${wipe.number}"

    private fun itemRarityLabel(rarity: EventHistoryItemRarity?): String =
        when (rarity) {
            EventHistoryItemRarity.MYTHICAL -> "Mythical"
            EventHistoryItemRarity.SECRET -> "Secret"
            null -> "Любая"
        }

    private fun itemTypeLabel(itemType: EventHistoryItemType?): String =
        when (itemType) {
            EventHistoryItemType.SWORD -> "⚔️ Мечи"
            EventHistoryItemType.AURA -> "✨ Ауры"
            EventHistoryItemType.PET -> "🐾 Петы"
            EventHistoryItemType.BOOK -> "📕 Книги"
            EventHistoryItemType.JEWELRY -> "💍 Бижутерия"
            null -> "Любой"
        }

    private fun boosterTypeLabel(boosterType: EventHistoryBoosterType?): String =
        when (boosterType) {
            EventHistoryBoosterType.LUCK -> "🍀 Удача"
            EventHistoryBoosterType.MONEY -> "💰 Деньги"
            EventHistoryBoosterType.DAMAGE -> "⚔️ Урон"
            EventHistoryBoosterType.POWER -> "💪 Сила"
            null -> "Любой"
        }

    private fun globalTypeLabel(globalType: EventHistoryGlobalType?): String =
        when (globalType) {
            EventHistoryGlobalType.DARKNESS -> "🌑 Тьма"
            EventHistoryGlobalType.MOON -> "🌙 Луна"
            EventHistoryGlobalType.ECLIPSE -> "🌘 Затмение"
            EventHistoryGlobalType.EXPLOSION -> "💥 Взрыв"
            EventHistoryGlobalType.COMET_CHAOS -> "☄️ Хаос"
            null -> "Любое"
        }

    private fun unavailableMessage(data: EventHistoryMenuScreenData.Feed? = null): OutboundTelegramMessage {
        val rows =
            if (data == null) {
                listOf(
                    listOf(refreshAction),
                    listOf(backAction),
                )
            } else {
                renderFeedRows(data, cursor = null)
            }

        return message(
            text =
                listOf(
                    "📜 <b>История событий</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "⚠️ Не удалось получить историю событий.",
                ).joinToString("\n"),
            rows = rows,
        )
    }

    private fun message(
        text: String,
        rows: List<List<OutboundTelegramMessageAction>>,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = text,
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )

    private fun callback(
        label: String,
        data: String,
        style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
            style = style,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"
    private const val RAID_LOCATIONS_PER_PAGE = 7
    private const val MAX_RAID_LOCATION = 28
    private const val LAST_RAID_LOCATION_PAGE = 3

    private val refreshAction = callback("🔄 Обновить", EventHistoryCallbackData.REFRESH)

    private val backAction = callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA)

    private val eventTimestampFormatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("dd.MM HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC)
}
