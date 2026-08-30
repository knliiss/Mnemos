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

object EventHistoryMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val historyData =
            data as? TelegramMenuScreenData.EventHistory
                ?: return unavailableMessage()

        return when (val result = historyData.data.result) {
            is EventHistoryResult.Available -> availableMessage(historyData.data, result)
            EventHistoryResult.Unavailable -> unavailableMessage()
        }
    }

    private fun availableMessage(
        data: EventHistoryMenuScreenData,
        result: EventHistoryResult.Available,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = renderAvailableText(data, result),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderAvailableActions(data, result),
        )

    private fun renderAvailableText(
        data: EventHistoryMenuScreenData,
        result: EventHistoryResult.Available,
    ): String {
        val filterLabel = filterLabel(data.filter)
        val eventLines =
            result.page.items
                .mapIndexed { index, item -> "${index + 1}. ${renderItem(item)}" }
                .joinToString("\n\n").ifBlank {
                    "Событий по выбранному фильтру пока нет."
                }

        return listOf(
            "🕘 <b>История событий</b>",
            "Фильтр: <b>$filterLabel</b>",
            "",
            eventLines,
        ).joinToString("\n")
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
                val locations = item.locations.sorted().joinToString(", ").ifBlank { "—" }

                "🛡 <b>Рейд</b> · локации ${escapeHtml(locations)}\n<code>$occurredAt</code>"
            }
        }
    }

    private fun renderAvailableActions(
        data: EventHistoryMenuScreenData,
        result: EventHistoryResult.Available,
    ): List<OutboundTelegramMessageAction> {
        val actions = filterActions(data.filter).toMutableList()

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

        result.page.cursor?.let { cursor ->
            actions +=
                OutboundTelegramMessageAction.Callback(
                    "Дальше →",
                    EventHistoryCallbackData.next(cursor),
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

    private fun unavailableMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "🕘 <b>История событий</b>",
                    "",
                    "Не удалось получить историю событий. Попробуйте обновить через несколько секунд.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions =
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        "🔄 Обновить",
                        EventHistoryCallbackData.REFRESH,
                    ),
                ) + commonNavigationActions,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

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
