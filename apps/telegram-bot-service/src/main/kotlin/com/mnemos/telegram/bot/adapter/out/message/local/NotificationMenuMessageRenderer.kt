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
            NotificationCreationMode.LIST -> renderDashboard(notificationsData.data)
            NotificationCreationMode.ITEM_DROP_LIST -> renderItemDropList(notificationsData.data)
            NotificationCreationMode.TYPE_SELECTION -> renderItemDropCreation(notificationsData.data)
            NotificationCreationMode.ITEM_DROP -> renderItemDropCreation(notificationsData.data)
            NotificationCreationMode.BOOSTER -> renderBoosterToggles(notificationsData.data)
            NotificationCreationMode.DUNGEON -> renderDungeonToggles(notificationsData.data)
            NotificationCreationMode.RAID -> renderRaidEditor(notificationsData.data)
            NotificationCreationMode.GLOBAL -> renderGlobalToggles(notificationsData.data)
        }
    }

    private fun renderDashboard(data: NotificationMenuScreenData): OutboundTelegramMessage =
        when (val result = data.result) {
            is NotificationSubscriptionResult.Available -> dashboardMessage(data, result)
            NotificationSubscriptionResult.Unavailable -> unavailableMessage(data.feedback)
            NotificationSubscriptionResult.NotLoaded -> unavailableMessage(data.feedback)
        }

    private fun dashboardMessage(
        data: NotificationMenuScreenData,
        result: NotificationSubscriptionResult.Available,
    ): OutboundTelegramMessage {
        val activeGlobals = result.globalSubscriptions.size
        val activeBoosters = result.boosterSubscriptions.size
        val activeDungeons = result.dungeonSubscriptions.size
        val raidLocations = raidLocationSummary(result)
        val dropRules = result.page.totalItems

        return messageWithRows(
            text =
                listOfNotNull(
                    "🔔 <b>Уведомления</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(data.feedback),
                    "",
                    "🌌 <b>Глобальные события</b>",
                    "└ Включено: <b>$activeGlobals/${NotificationGlobalType.entries.size}</b>",
                    "",
                    "⚡ <b>Бустеры</b>",
                    "└ Включено: <b>$activeBoosters/${NotificationBoosterType.entries.size}</b>",
                    "",
                    "🏰 <b>Dungeon</b>",
                    "└ Включено: <b>$activeDungeons/${NotificationDungeonType.entries.size}</b>",
                    "",
                    "🛡 <b>Рейды</b>",
                    "└ $raidLocations",
                    "",
                    "📦 <b>Дропы</b>",
                    "└ Правил: <b>$dropRules</b>",
                ).joinToString("\n"),
            rows = dashboardRows(result),
        )
    }

    private fun dashboardRows(result: NotificationSubscriptionResult.Available): List<List<OutboundTelegramMessageAction>> {
        val globalActions =
            listOf(
                globalToggle(result, NotificationGlobalType.DARKNESS, "🌑 Тьма"),
                globalToggle(result, NotificationGlobalType.MOON, "🌙 Луна"),
                globalToggle(result, NotificationGlobalType.ECLIPSE, "🌘 Затмение"),
                globalToggle(result, NotificationGlobalType.EXPLOSION, "💥 Взрыв"),
                globalToggle(result, NotificationGlobalType.COMET_CHAOS, "☄️ Хаос"),
            )
        val boosterActions =
            listOf(
                boosterToggle(result, NotificationBoosterType.LUCK, "🍀 Удача"),
                boosterToggle(result, NotificationBoosterType.MONEY, "💰 Деньги"),
                boosterToggle(result, NotificationBoosterType.DAMAGE, "⚔️ Урон"),
                boosterToggle(result, NotificationBoosterType.POWER, "💪 Сила"),
            )
        val dungeonActions =
            listOf(
                dungeonToggle(result, NotificationDungeonType.EASY, "🟢 Легкий"),
                dungeonToggle(result, NotificationDungeonType.MEDIUM, "🟡 Средний"),
                dungeonToggle(result, NotificationDungeonType.HARD, "🔴 Сложный"),
                dungeonToggle(result, NotificationDungeonType.EXPERT, "🟣 Эксперт"),
            )
        val raidActive = result.raidSubscription != null
        val raidAction =
            callback(
                label = stateLabel("🛡 Рейды", raidActive),
                data = NotificationCallbackData.type(NotificationSubscriptionType.RAID),
                style = activeStyle(raidActive),
            )
        val dropsAction =
            callback(
                label = "📦 Дропы · ${result.page.totalItems}",
                data = NotificationCallbackData.DROPS,
                style = OutboundTelegramButtonStyle.PRIMARY,
            )

        return buildList {
            addAll(globalActions.chunked(2))
            addAll(boosterActions.chunked(2))
            addAll(dungeonActions.chunked(2))
            add(listOf(raidAction))
            add(listOf(dropsAction))
            add(listOf(refreshAction))
            add(listOf(backAction))
        }
    }

    private fun renderItemDropList(data: NotificationMenuScreenData): OutboundTelegramMessage =
        when (val result = data.result) {
            is NotificationSubscriptionResult.Available -> itemDropListMessage(data, result)
            NotificationSubscriptionResult.Unavailable -> unavailableMessage(data.feedback)
            NotificationSubscriptionResult.NotLoaded -> unavailableMessage(data.feedback)
        }

    private fun itemDropListMessage(
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
                    "Правил дропа пока нет."
                }

        return messageWithRows(
            text =
                listOfNotNull(
                    "📦 <b>Дропы</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(data.feedback),
                    "",
                    "Правил: <b>${page.totalItems}</b>",
                    "Страница: <b>${page.page + 1}/$totalPages</b>",
                    "",
                    subscriptionLines,
                ).joinToString("\n"),
            rows = renderItemDropListRows(result),
        )
    }

    private fun renderItemDropListRows(result: NotificationSubscriptionResult.Available): List<List<OutboundTelegramMessageAction>> {
        val page = result.page
        val totalPages = page.totalPages.coerceAtLeast(1)
        val rows = mutableListOf<List<OutboundTelegramMessageAction>>()

        rows +=
            listOf(
                callback(
                    label = "➕ Новое правило",
                    data = NotificationCallbackData.CREATE,
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
            )

        page.items
            .mapIndexed { index, subscription ->
                callback(
                    label = "🗑 ${index + 1}",
                    data = NotificationCallbackData.delete(subscription.id),
                    style = OutboundTelegramButtonStyle.DANGER,
                )
            }.chunked(DELETE_BUTTONS_PER_ROW)
            .forEach(rows::add)

        val pageRow = mutableListOf<OutboundTelegramMessageAction>()

        if (page.page > 0) {
            pageRow += callback("◀ ${page.page}/$totalPages", NotificationCallbackData.page(page.page - 1))
        }

        if (page.hasNextPage) {
            pageRow += callback("${page.page + 2}/$totalPages ▶", NotificationCallbackData.page(page.page + 1))
        }

        if (pageRow.isNotEmpty()) {
            rows += pageRow
        }

        rows += listOf(refreshAction)
        rows += listOf(notificationsAction)

        return rows
    }

    private fun renderItemDropCreation(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val playerFilter = data.droppedFor?.let { value -> "<code>${escapeHtml(value)}</code>" } ?: "—"
        val itemFilter = data.itemName?.let { value -> "<code>${escapeHtml(value)}</code>" } ?: "—"
        val inputPrompt =
            when (data.itemDropInput) {
                NotificationItemDropInputField.PLAYER -> "👤 <b>4–20</b> · <code>A-Z a-z 0-9 _</code>"
                NotificationItemDropInputField.ITEM -> "⚔️ <b>1–24</b>"
                null -> "Укажите игрока, предмет или оба фильтра."
            }

        return messageWithRows(
            text =
                listOfNotNull(
                    "📦 <b>Новое правило дропа</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(data.feedback),
                    "",
                    "👤 Игрок: $playerFilter",
                    "⚔️ Предмет: $itemFilter",
                    "",
                    inputPrompt,
                ).joinToString("\n"),
            rows = renderItemDropRows(data),
        )
    }

    private fun renderItemDropRows(data: NotificationMenuScreenData): List<List<OutboundTelegramMessageAction>> {
        if (data.itemDropInput != null) {
            return listOf(
                listOf(
                    callback(
                        label = "✖ Отмена",
                        data = NotificationCallbackData.ITEM_DROP_INPUT_CANCEL,
                        style = OutboundTelegramButtonStyle.DANGER,
                    ),
                ),
            )
        }

        val rows =
            mutableListOf<List<OutboundTelegramMessageAction>>(
                listOf(
                    callback("👤 Игрок", NotificationCallbackData.ITEM_DROP_PLAYER),
                    callback("⚔️ Предмет", NotificationCallbackData.ITEM_DROP_ITEM),
                ),
            )
        val clearRow = mutableListOf<OutboundTelegramMessageAction>()

        if (data.droppedFor != null) {
            clearRow +=
                callback(
                    "🗑 Игрок",
                    NotificationCallbackData.ITEM_DROP_CLEAR_PLAYER,
                    OutboundTelegramButtonStyle.DANGER,
                )
        }

        if (data.itemName != null) {
            clearRow +=
                callback(
                    "🗑 Предмет",
                    NotificationCallbackData.ITEM_DROP_CLEAR_ITEM,
                    OutboundTelegramButtonStyle.DANGER,
                )
        }

        if (clearRow.isNotEmpty()) {
            rows += clearRow
        }

        if (data.droppedFor != null || data.itemName != null) {
            rows +=
                listOf(
                    callback(
                        "💾 Создать правило",
                        NotificationCallbackData.ITEM_DROP_CREATE,
                        OutboundTelegramButtonStyle.SUCCESS,
                    ),
                )
        }

        rows += listOf(dropsAction)

        return rows
    }

    private fun renderBoosterToggles(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val available = data.result as? NotificationSubscriptionResult.Available ?: return unavailableMessage(data.feedback)
        val actions =
            listOf(
                boosterToggle(available, NotificationBoosterType.LUCK, "🍀 Удача"),
                boosterToggle(available, NotificationBoosterType.MONEY, "💰 Деньги"),
                boosterToggle(available, NotificationBoosterType.DAMAGE, "⚔️ Урон"),
                boosterToggle(available, NotificationBoosterType.POWER, "💪 Сила"),
            )

        return renderToggleScreen(
            data = data,
            title = "⚡ <b>Бустеры</b>",
            toggleRows = actions.chunked(2),
        )
    }

    private fun renderDungeonToggles(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val available = data.result as? NotificationSubscriptionResult.Available ?: return unavailableMessage(data.feedback)
        val actions =
            listOf(
                dungeonToggle(available, NotificationDungeonType.EASY, "🟢 Легкий"),
                dungeonToggle(available, NotificationDungeonType.MEDIUM, "🟡 Средний"),
                dungeonToggle(available, NotificationDungeonType.HARD, "🔴 Сложный"),
                dungeonToggle(available, NotificationDungeonType.EXPERT, "🟣 Эксперт"),
            )

        return renderToggleScreen(
            data = data,
            title = "🏰 <b>Dungeon</b>",
            toggleRows = actions.chunked(2),
        )
    }

    private fun renderGlobalToggles(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val available = data.result as? NotificationSubscriptionResult.Available ?: return unavailableMessage(data.feedback)
        val actions =
            listOf(
                globalToggle(available, NotificationGlobalType.DARKNESS, "🌑 Тьма"),
                globalToggle(available, NotificationGlobalType.MOON, "🌙 Луна"),
                globalToggle(available, NotificationGlobalType.ECLIPSE, "🌘 Затмение"),
                globalToggle(available, NotificationGlobalType.EXPLOSION, "💥 Взрыв"),
                globalToggle(available, NotificationGlobalType.COMET_CHAOS, "☄️ Хаос"),
            )

        return renderToggleScreen(
            data = data,
            title = "🌌 <b>Глобальные события</b>",
            toggleRows = actions.chunked(2),
        )
    }

    private fun renderToggleScreen(
        data: NotificationMenuScreenData,
        title: String,
        toggleRows: List<List<OutboundTelegramMessageAction>>,
    ): OutboundTelegramMessage =
        messageWithRows(
            text =
                listOfNotNull(
                    title,
                    MESSAGE_DIVIDER,
                    feedbackText(data.feedback),
                ).joinToString("\n"),
            rows = toggleRows + listOf(listOf(notificationsAction)),
        )

    private fun renderRaidEditor(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val available = data.result as? NotificationSubscriptionResult.Available ?: return unavailableMessage(data.feedback)
        val raidSubscription = available.raidSubscription

        if (raidSubscription == null) {
            return messageWithRows(
                text =
                    "🛡 <b>Рейды</b>\n$MESSAGE_DIVIDER\n\n" +
                        "Существующая RAID-подписка не найдена. Новая подписка из этого экрана не создаётся.",
                rows = listOf(listOf(notificationsAction)),
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
                compactToggleCallback(
                    label = location.toString(),
                    active = location in data.raidLocations,
                    targetData = { selected -> NotificationCallbackData.raidLocation(location, selected) },
                )
            }
        val pageActions =
            when (data.raidPage) {
                0 -> listOf(callback("2/2 ▶", NotificationCallbackData.raidPage(1)))
                else -> listOf(callback("◀ 1/2", NotificationCallbackData.raidPage(0)))
            }
        val anyLocationAction =
            compactStateCallback(
                label = "◎ Любая локация",
                data = NotificationCallbackData.RAID_CLEAR,
                active = data.raidLocations.isEmpty(),
            )
        val saveAction =
            callback(
                label = "💾 Сохранить RAID",
                data = NotificationCallbackData.RAID_SAVE,
                style = OutboundTelegramButtonStyle.SUCCESS,
            )

        return messageWithRows(
            text =
                listOfNotNull(
                    "🛡 <b>Рейды</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(data.feedback),
                    "",
                    "📍 Локации: <b>${escapeHtml(selectedLocations)}</b>",
                ).joinToString("\n"),
            rows =
                buildList {
                    addAll(locationActions.chunked(RAID_LOCATION_BUTTONS_PER_ROW))
                    add(pageActions)
                    add(listOf(anyLocationAction))
                    add(listOf(saveAction))
                    add(listOf(notificationsAction))
                },
        )
    }

    private fun globalToggle(
        result: NotificationSubscriptionResult.Available,
        type: NotificationGlobalType,
        label: String,
    ): OutboundTelegramMessageAction.Callback {
        val active = result.globalSubscriptions.containsKey(type)

        return targetToggleCallback(
            label = label,
            active = active,
            targetData = { enabled -> NotificationCallbackData.global(type, enabled) },
        )
    }

    private fun boosterToggle(
        result: NotificationSubscriptionResult.Available,
        type: NotificationBoosterType,
        label: String,
    ): OutboundTelegramMessageAction.Callback {
        val active = result.boosterSubscriptions.containsKey(type)

        return targetToggleCallback(
            label = label,
            active = active,
            targetData = { enabled -> NotificationCallbackData.booster(type, enabled) },
        )
    }

    private fun dungeonToggle(
        result: NotificationSubscriptionResult.Available,
        type: NotificationDungeonType,
        label: String,
    ): OutboundTelegramMessageAction.Callback {
        val active = result.dungeonSubscriptions.containsKey(type)

        return targetToggleCallback(
            label = label,
            active = active,
            targetData = { enabled -> NotificationCallbackData.dungeon(type, enabled) },
        )
    }

    private fun raidLocationSummary(result: NotificationSubscriptionResult.Available): String {
        val raid = result.raidSubscription ?: return "Выкл"

        return if (raid.raidLocations.isEmpty()) {
            "Вкл · любая локация"
        } else {
            "Вкл · ${raid.raidLocations.sorted().joinToString(", ")}"
        }
    }

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

    private fun feedbackText(feedback: NotificationMenuFeedback?): String? =
        when (feedback) {
            NotificationMenuFeedback.CREATED -> "✅ Правило сохранено."
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
        messageWithRows(
            text =
                listOfNotNull(
                    "🔔 <b>Уведомления</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(feedback),
                    "",
                    "Не удалось получить настройки уведомлений.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(refreshAction),
                    listOf(backAction),
                ),
        )

    private fun messageWithRows(
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

    private fun targetToggleCallback(
        label: String,
        active: Boolean,
        targetData: (Boolean) -> String,
    ): OutboundTelegramMessageAction.Callback =
        callback(
            label = stateLabel(label, active),
            data = targetData(!active),
            style = activeStyle(active),
        )

    private fun compactToggleCallback(
        label: String,
        active: Boolean,
        targetData: (Boolean) -> String,
    ): OutboundTelegramMessageAction.Callback =
        compactStateCallback(
            label = label,
            data = targetData(!active),
            active = active,
        )

    private fun compactStateCallback(
        label: String,
        data: String,
        active: Boolean,
    ): OutboundTelegramMessageAction.Callback =
        callback(
            label = label,
            data = data,
            style = activeStyle(active),
        )

    private fun stateLabel(
        label: String,
        active: Boolean,
    ): String = "$label ${if (active) "Вкл" else "Выкл"}"

    private fun activeStyle(active: Boolean): OutboundTelegramButtonStyle =
        if (active) {
            OutboundTelegramButtonStyle.SUCCESS
        } else {
            OutboundTelegramButtonStyle.DEFAULT
        }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"
    private const val DELETE_BUTTONS_PER_ROW = 4
    private const val RAID_LOCATIONS_PER_PAGE = 14
    private const val RAID_LOCATION_BUTTONS_PER_ROW = 7
    private const val MAX_RAID_LOCATION = 28

    private val refreshAction = callback("🔄 Обновить", NotificationCallbackData.REFRESH)
    private val backAction = callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA)
    private val notificationsAction = callback("← Уведомления", TelegramMenuScreen.NOTIFICATIONS.callbackData)
    private val dropsAction = callback("← Дропы", NotificationCallbackData.DROPS)
}
