package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.events.EventHistoryCallbackData
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
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
            )

        data.search?.let { search ->
            lines += "Поиск: <code>${escapeHtml(search)}</code>"
        }

        lines += ""
        lines += eventLines

        return lines.joinToString("\n")
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

    private fun filterActions(selected: EventHistoryFilter): List<OutboundTelegramMessageAction> =
        EventHistoryFilter.entries.map { filter ->
            OutboundTelegramMessageAction.Callback(
                label = filterButtonLabel(filter, selected),
                data = EventHistoryCallbackData.filter(filter),
            )
        }

    private fun filterButtonLabel(
        filter: EventHistoryFilter,
        selected: EventHistoryFilter,
    ): String {
        val prefix = if (filter == selected) "✓ " else ""
        return prefix + filterLabel(filter)
    }

    private fun filterLabel(filter: EventHistoryFilter): String =
        when (filter) {
            EventHistoryFilter.ALL -> "Все"
            EventHistoryFilter.ITEM_DROP -> "Дропы"
            EventHistoryFilter.BOOSTER -> "Бустеры"
            EventHistoryFilter.GLOBAL -> "Глобальные"
            EventHistoryFilter.RAID -> "Рейды"
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
}
