package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.notification.NotificationCallbackData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType

object NotificationMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val notificationsData =
            data as? TelegramMenuScreenData.Notifications
                ?: return unavailableMessage()

        return when (val result = notificationsData.data.result) {
            is NotificationSubscriptionResult.Available -> availableMessage(result)
            NotificationSubscriptionResult.Unavailable -> unavailableMessage()
        }
    }

    private fun availableMessage(result: NotificationSubscriptionResult.Available): OutboundTelegramMessage {
        val page = result.page
        val subscriptionLines =
            page.items
                .mapIndexed { index, subscription ->
                    "${index + 1}. ${renderSubscription(subscription)}"
                }.joinToString("\n\n")
                .ifBlank {
                    "Активных подписок пока нет."
                }
        val pageLine =
            if (page.totalPages > 0) {
                "Страница: <b>${page.page + 1}/${page.totalPages}</b>"
            } else {
                "Страница: <b>1/1</b>"
            }
        val text =
            listOf(
                "🔔 <b>Уведомления</b>",
                "Активных подписок: <b>${page.totalItems}</b>",
                pageLine,
                "",
                subscriptionLines,
                deleteHint(page.items),
            ).filter(String::isNotBlank)
                .joinToString("\n")

        return OutboundTelegramMessage(
            text = text,
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderActions(page.items, page.page, page.hasNextPage),
        )
    }

    private fun renderSubscription(subscription: NotificationSubscription): String =
        when (subscription.type) {
            NotificationSubscriptionType.ITEM_DROP -> renderItemDrop(subscription)
            NotificationSubscriptionType.BOOSTER -> "⚡ <b>Бустер</b> · ${displayValue(subscription.boosterType)}"
            NotificationSubscriptionType.DUNGEON -> "🏰 <b>Данж</b> · ${displayValue(subscription.dungeonType)}"
            NotificationSubscriptionType.RAID -> renderRaid(subscription)
            NotificationSubscriptionType.GLOBAL -> "🌒 <b>Глобальное</b> · ${displayValue(subscription.globalType)}"
        }

    private fun renderItemDrop(subscription: NotificationSubscription): String {
        val filters =
            listOfNotNull(
                subscription.itemName?.takeIf(String::isNotBlank)?.let { itemName ->
                    "предмет <code>${escapeHtml(itemName)}</code>"
                },
                subscription.droppedFor?.takeIf(String::isNotBlank)?.let { nickname ->
                    "игрок <code>${escapeHtml(nickname)}</code>"
                },
            ).joinToString(" · ")

        return "⚔️ <b>Дроп</b> · ${filters.ifBlank { "без фильтра" }}"
    }

    private fun renderRaid(subscription: NotificationSubscription): String {
        val locations =
            subscription.raidLocations
                .sorted()
                .joinToString(", ")
                .ifBlank { "любая" }

        return "🛡 <b>Рейд</b> · локации ${escapeHtml(locations)}"
    }

    private fun deleteHint(items: List<NotificationSubscription>): String =
        if (items.isEmpty()) {
            ""
        } else {
            "\nДля удаления нажмите ✖ с номером подписки."
        }

    private fun renderActions(
        items: List<NotificationSubscription>,
        page: Int,
        hasNextPage: Boolean,
    ): List<OutboundTelegramMessageAction> {
        val actions = mutableListOf<OutboundTelegramMessageAction>()

        items.forEachIndexed { index, subscription ->
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "✖ ${index + 1}",
                    data = NotificationCallbackData.delete(subscription.id),
                )
        }

        if (page > 0) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "← Страница",
                    data = NotificationCallbackData.page(page - 1),
                )
        }

        if (hasNextPage) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "Страница →",
                    data = NotificationCallbackData.page(page + 1),
                )
        }

        actions += refreshAction
        actions += navigationActions

        return actions
    }

    private fun displayValue(value: String?): String =
        value
            ?.takeIf(String::isNotBlank)
            ?.lowercase()
            ?.replace('_', ' ')
            ?.replaceFirstChar { character -> character.titlecase() }
            ?.let(::escapeHtml)
            ?: "—"

    private fun unavailableMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "🔔 <b>Уведомления</b>",
                    "",
                    "Не удалось получить подписки. Попробуйте обновить через несколько секунд.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = listOf(refreshAction) + navigationActions,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private val refreshAction =
        OutboundTelegramMessageAction.Callback(
            "🔄 Обновить",
            NotificationCallbackData.REFRESH,
        )

    private val navigationActions =
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
}
