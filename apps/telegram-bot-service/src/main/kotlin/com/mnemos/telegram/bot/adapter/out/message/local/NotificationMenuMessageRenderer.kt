package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.notification.NotificationCallbackData
import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropInputField
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuFeedback
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType

object NotificationMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val notificationsData =
            data as? TelegramMenuScreenData.Notifications
                ?: return unavailableMessage()

        return when (notificationsData.data.mode) {
            NotificationCreationMode.LIST -> renderList(notificationsData.data)
            NotificationCreationMode.TYPE_SELECTION -> renderItemDropCreation(notificationsData.data)
            NotificationCreationMode.ITEM_DROP -> renderItemDropCreation(notificationsData.data)
            NotificationCreationMode.BOOSTER -> renderBoosterToggles(notificationsData.data)
            NotificationCreationMode.DUNGEON -> renderDungeonToggles(notificationsData.data)
            NotificationCreationMode.RAID -> renderRaidEditor(notificationsData.data)
            NotificationCreationMode.GLOBAL -> renderGlobalToggles(notificationsData.data)
        }
    }

    private fun renderList(data: NotificationMenuScreenData): OutboundTelegramMessage =
        when (val result = data.result) {
            is NotificationSubscriptionResult.Available -> availableListMessage(data, result)
            NotificationSubscriptionResult.Unavailable -> unavailableMessage(data.feedback)
            NotificationSubscriptionResult.NotLoaded -> unavailableMessage(data.feedback)
        }

    private fun availableListMessage(
        data: NotificationMenuScreenData,
        result: NotificationSubscriptionResult.Available,
    ): OutboundTelegramMessage {
        val page = result.page
        val totalPages = page.totalPages.coerceAtLeast(1)
        val subscriptionLines =
            page.items
                .mapIndexed { index, subscription ->
                    "${index + 1}. ${renderItemDrop(subscription)}"
                }.joinToString("\n\n")
                .ifBlank {
                    "Активных подписок пока нет."
                }
        val text =
            listOfNotNull(
                "🔔 <b>Уведомления</b>",
                "⚔️ <b>Дропы</b>",
                feedbackText(data.feedback),
                "Активных подписок: <b>${page.totalItems}</b>",
                "Страница: <b>${page.page + 1}/$totalPages</b>",
                "",
                subscriptionLines,
                deleteHint(page.items),
            ).filter(String::isNotBlank)
                .joinToString("\n")

        return message(
            text = text,
            actions = renderListActions(result),
        )
    }

    private fun renderListActions(result: NotificationSubscriptionResult.Available): List<OutboundTelegramMessageAction> {
        val page = result.page
        val totalPages = page.totalPages.coerceAtLeast(1)
        val actions =
            mutableListOf<OutboundTelegramMessageAction>(
                callback(
                    label = "➕ Подписка на дроп",
                    data = NotificationCallbackData.CREATE,
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
                callback(
                    label = "⚡ Бустеры",
                    data = NotificationCallbackData.type(NotificationSubscriptionType.BOOSTER),
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
                callback(
                    label = "🏰 Dungeon",
                    data = NotificationCallbackData.type(NotificationSubscriptionType.DUNGEON),
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
                callback(
                    label = "🛡 Рейды",
                    data = NotificationCallbackData.type(NotificationSubscriptionType.RAID),
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
                callback(
                    label = "🌒 Глобальные",
                    data = NotificationCallbackData.type(NotificationSubscriptionType.GLOBAL),
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
            )

        page.items.forEachIndexed { index, subscription ->
            actions +=
                callback(
                    label = "✖ ${index + 1}",
                    data = NotificationCallbackData.delete(subscription.id),
                    style = OutboundTelegramButtonStyle.DANGER,
                )
        }

        if (page.page > 0) {
            actions += callback("← ${page.page}/$totalPages", NotificationCallbackData.page(page.page - 1))
        }

        if (page.hasNextPage) {
            actions += callback("${page.page + 2}/$totalPages →", NotificationCallbackData.page(page.page + 1))
        }

        actions += refreshAction
        actions += navigationActions

        return actions
    }

    private fun renderItemDropCreation(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val playerFilter = data.droppedFor?.let { value -> "<code>${escapeHtml(value)}</code>" } ?: "—"
        val itemFilter = data.itemName?.let { value -> "<code>${escapeHtml(value)}</code>" } ?: "—"
        val inputPrompt =
            when (data.itemDropInput) {
                NotificationItemDropInputField.PLAYER -> "👤 <b>4–20</b> · <code>A-Z a-z 0-9 _</code>"
                NotificationItemDropInputField.ITEM -> "⚔️ <b>1–24</b>"
                null -> "Укажите хотя бы один фильтр. Можно использовать оба одновременно."
            }

        return message(
            text =
                listOfNotNull(
                    "⚔️ <b>Подписка на дроп</b>",
                    feedbackText(data.feedback),
                    "",
                    "👤 $playerFilter",
                    "⚔️ $itemFilter",
                    "",
                    inputPrompt,
                ).joinToString("\n"),
            actions = renderItemDropActions(data),
        )
    }

    private fun renderItemDropActions(data: NotificationMenuScreenData): List<OutboundTelegramMessageAction> {
        if (data.itemDropInput != null) {
            return listOf(
                callback(
                    label = "✖ Отмена",
                    data = NotificationCallbackData.ITEM_DROP_INPUT_CANCEL,
                    style = OutboundTelegramButtonStyle.DANGER,
                ),
                homeAction,
            )
        }

        val actions =
            mutableListOf<OutboundTelegramMessageAction>(
                callback("👤", NotificationCallbackData.ITEM_DROP_PLAYER),
                callback("⚔️", NotificationCallbackData.ITEM_DROP_ITEM),
            )

        if (data.droppedFor != null) {
            actions += callback("✖ 👤", NotificationCallbackData.ITEM_DROP_CLEAR_PLAYER, OutboundTelegramButtonStyle.DANGER)
        }

        if (data.itemName != null) {
            actions += callback("✖ ⚔️", NotificationCallbackData.ITEM_DROP_CLEAR_ITEM, OutboundTelegramButtonStyle.DANGER)
        }

        if (data.droppedFor != null || data.itemName != null) {
            actions += callback("✓ Создать", NotificationCallbackData.ITEM_DROP_CREATE, OutboundTelegramButtonStyle.SUCCESS)
        }

        actions += editorNavigationActions

        return actions
    }

    private fun renderBoosterToggles(data: NotificationMenuScreenData): OutboundTelegramMessage =
        renderToggleScreen(
            data = data,
            title = "⚡ <b>Подписка на бустер</b>",
            actions =
                listOf(
                    toggleCallback(
                        label = "🍀 Удача",
                        data = NotificationCallbackData.booster(NotificationBoosterType.LUCK),
                        active = isBoosterActive(data, NotificationBoosterType.LUCK),
                    ),
                    toggleCallback(
                        label = "💰 Деньги",
                        data = NotificationCallbackData.booster(NotificationBoosterType.MONEY),
                        active = isBoosterActive(data, NotificationBoosterType.MONEY),
                    ),
                    toggleCallback(
                        label = "⚔️ Урон",
                        data = NotificationCallbackData.booster(NotificationBoosterType.DAMAGE),
                        active = isBoosterActive(data, NotificationBoosterType.DAMAGE),
                    ),
                    toggleCallback(
                        label = "💪 Сила",
                        data = NotificationCallbackData.booster(NotificationBoosterType.POWER),
                        active = isBoosterActive(data, NotificationBoosterType.POWER),
                    ),
                ),
        )

    private fun renderDungeonToggles(data: NotificationMenuScreenData): OutboundTelegramMessage =
        renderToggleScreen(
            data = data,
            title = "🏰 <b>Подписка на данж</b>",
            actions =
                listOf(
                    toggleCallback(
                        label = "Легкий",
                        data = NotificationCallbackData.dungeon(NotificationDungeonType.EASY),
                        active = isDungeonActive(data, NotificationDungeonType.EASY),
                    ),
                    toggleCallback(
                        label = "Средний",
                        data = NotificationCallbackData.dungeon(NotificationDungeonType.MEDIUM),
                        active = isDungeonActive(data, NotificationDungeonType.MEDIUM),
                    ),
                    toggleCallback(
                        label = "Сложный",
                        data = NotificationCallbackData.dungeon(NotificationDungeonType.HARD),
                        active = isDungeonActive(data, NotificationDungeonType.HARD),
                    ),
                    toggleCallback(
                        label = "Эксперт",
                        data = NotificationCallbackData.dungeon(NotificationDungeonType.EXPERT),
                        active = isDungeonActive(data, NotificationDungeonType.EXPERT),
                    ),
                ),
        )

    private fun renderGlobalToggles(data: NotificationMenuScreenData): OutboundTelegramMessage =
        renderToggleScreen(
            data = data,
            title = "🌒 <b>Подписка на глобальное событие</b>",
            actions =
                listOf(
                    toggleCallback(
                        label = "🌑 Тьма",
                        data = NotificationCallbackData.global(NotificationGlobalType.DARKNESS),
                        active = isGlobalActive(data, NotificationGlobalType.DARKNESS),
                    ),
                    toggleCallback(
                        label = "🌙 Луна",
                        data = NotificationCallbackData.global(NotificationGlobalType.MOON),
                        active = isGlobalActive(data, NotificationGlobalType.MOON),
                    ),
                    toggleCallback(
                        label = "🌘 Затмение",
                        data = NotificationCallbackData.global(NotificationGlobalType.ECLIPSE),
                        active = isGlobalActive(data, NotificationGlobalType.ECLIPSE),
                    ),
                    toggleCallback(
                        label = "💥 Взрыв",
                        data = NotificationCallbackData.global(NotificationGlobalType.EXPLOSION),
                        active = isGlobalActive(data, NotificationGlobalType.EXPLOSION),
                    ),
                    toggleCallback(
                        label = "☄️ COMET_CHAOS",
                        data = NotificationCallbackData.global(NotificationGlobalType.COMET_CHAOS),
                        active = isGlobalActive(data, NotificationGlobalType.COMET_CHAOS),
                    ),
                ),
        )

    private fun renderToggleScreen(
        data: NotificationMenuScreenData,
        title: String,
        actions: List<OutboundTelegramMessageAction>,
    ): OutboundTelegramMessage {
        if (data.result !is NotificationSubscriptionResult.Available) {
            return unavailableMessage(data.feedback)
        }

        return message(
            text =
                listOfNotNull(
                    title,
                    feedbackText(data.feedback),
                ).joinToString("\n"),
            actions = actions + editorNavigationActions,
        )
    }

    private fun renderRaidEditor(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val available = data.result as? NotificationSubscriptionResult.Available ?: return unavailableMessage(data.feedback)
        val raidSubscription = available.raidSubscription

        if (raidSubscription == null) {
            return message(
                text = "⚠️ <b>RAID</b>\n\nMini App",
                actions =
                    listOf(
                        OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
                    ) + editorNavigationActions,
            )
        }

        val selectedLocations =
            if (data.raidLocations.isEmpty()) {
                "Любая локация"
            } else {
                data.raidLocations.sorted().joinToString(", ")
            }
        val firstLocation = data.raidPage * RAID_LOCATIONS_PER_PAGE + 1
        val lastLocation = (firstLocation + RAID_LOCATIONS_PER_PAGE - 1).coerceAtMost(MAX_RAID_LOCATION)
        val locationActions =
            (firstLocation..lastLocation).map { location ->
                toggleCallback(
                    label = location.toString(),
                    data = NotificationCallbackData.raidLocation(location),
                    active = location in data.raidLocations,
                )
            }
        val pageActions =
            when (data.raidPage) {
                0 -> listOf(callback("2/2 →", NotificationCallbackData.raidPage(1)))
                else -> listOf(callback("← 1/2", NotificationCallbackData.raidPage(0)))
            }
        val anyLocationAction =
            toggleCallback(
                label = "◎ Любая локация",
                data = NotificationCallbackData.RAID_CLEAR,
                active = data.raidLocations.isEmpty(),
            )
        val saveAction =
            callback(
                label = "✅ RAID",
                data = NotificationCallbackData.RAID_SAVE,
                style = OutboundTelegramButtonStyle.SUCCESS,
            )

        return message(
            text =
                listOfNotNull(
                    "🛡 <b>Подписка на рейд</b>",
                    feedbackText(data.feedback),
                    "",
                    "📍 <b>${escapeHtml(selectedLocations)}</b>",
                    "",
                    "Если ничего не выбрано, уведомления будут приходить для рейда на любой локации.",
                ).joinToString("\n"),
            actions = locationActions + pageActions + anyLocationAction + saveAction + editorNavigationActions,
        )
    }

    private fun isBoosterActive(
        data: NotificationMenuScreenData,
        type: NotificationBoosterType,
    ): Boolean =
        (data.result as? NotificationSubscriptionResult.Available)
            ?.boosterSubscriptions
            ?.containsKey(type)
            ?: false

    private fun isDungeonActive(
        data: NotificationMenuScreenData,
        type: NotificationDungeonType,
    ): Boolean =
        (data.result as? NotificationSubscriptionResult.Available)
            ?.dungeonSubscriptions
            ?.containsKey(type)
            ?: false

    private fun isGlobalActive(
        data: NotificationMenuScreenData,
        type: NotificationGlobalType,
    ): Boolean =
        (data.result as? NotificationSubscriptionResult.Available)
            ?.globalSubscriptions
            ?.containsKey(type)
            ?: false

    private fun renderItemDrop(subscription: NotificationSubscription): String {
        val filters =
            listOfNotNull(
                subscription.itemName
                    ?.takeIf(String::isNotBlank)
                    ?.let { itemName -> "⚔️ <code>${escapeHtml(itemName)}</code>" },
                subscription.droppedFor
                    ?.takeIf(String::isNotBlank)
                    ?.let { nickname -> "👤 <code>${escapeHtml(nickname)}</code>" },
            ).joinToString(" · ")

        return filters.ifBlank { "⚔️ —" }
    }

    private fun deleteHint(items: List<NotificationSubscription>): String =
        if (items.isEmpty()) {
            ""
        } else {
            "\nДля удаления нажмите ✖ с номером подписки."
        }

    private fun feedbackText(feedback: NotificationMenuFeedback?): String? =
        when (feedback) {
            NotificationMenuFeedback.CREATED -> "✅ Подписка активирована."
            NotificationMenuFeedback.USER_NOT_FOUND ->
                "⚠️ Не удалось подготовить профиль Mnemos. Попробуйте еще раз."

            NotificationMenuFeedback.SERVICE_UNAVAILABLE ->
                "⚠️ Сервис уведомлений временно недоступен. Попробуйте еще раз."

            NotificationMenuFeedback.INVALID_PLAYER_FILTER ->
                "⚠️ Ник должен содержать 4–20 символов: буквы, цифры или <code>_</code>."

            NotificationMenuFeedback.INVALID_ITEM_FILTER ->
                "⚠️ Название предмета должно содержать от 1 до 24 символов."

            NotificationMenuFeedback.ITEM_DROP_FILTER_REQUIRED ->
                "⚠️ Укажите игрока, предмет или оба фильтра."

            null -> null
        }

    private fun unavailableMessage(feedback: NotificationMenuFeedback? = null): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "🔔 <b>Уведомления</b>",
                    feedbackText(feedback),
                    "",
                    "Не удалось получить подписки. Попробуйте обновить через несколько секунд.",
                ).joinToString("\n"),
            actions = listOf(refreshAction) + navigationActions,
        )

    private fun message(
        text: String,
        actions: List<OutboundTelegramMessageAction>,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = text,
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = actions,
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

    private fun toggleCallback(
        label: String,
        data: String,
        active: Boolean,
    ): OutboundTelegramMessageAction.Callback =
        callback(
            label = if (active) "✅ $label" else label,
            data = data,
            style = if (active) OutboundTelegramButtonStyle.SUCCESS else OutboundTelegramButtonStyle.DEFAULT,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private const val RAID_LOCATIONS_PER_PAGE = 14
    private const val MAX_RAID_LOCATION = 28

    private val refreshAction = callback("🔄 Обновить", NotificationCallbackData.REFRESH)
    private val homeAction = callback("⌂ Меню", TelegramMenuScreen.HOME.callbackData)

    private val editorNavigationActions =
        listOf(
            callback("← Уведомления", TelegramMenuScreen.NOTIFICATIONS.callbackData),
            homeAction,
        )

    private val navigationActions =
        listOf(
            callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA),
            homeAction,
        )
}
