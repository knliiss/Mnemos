package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.notification.NotificationCallbackData
import com.mnemos.telegram.bot.application.notification.NotificationRaidLocations
import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropInputField
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuFeedback
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult

object NotificationMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val notificationsData =
            data as? TelegramMenuScreenData.Notifications
                ?: return unavailableMessage()

        return when (notificationsData.data.mode) {
            NotificationCreationMode.LIST -> renderDashboard(notificationsData.data)
            NotificationCreationMode.GLOBALS -> renderGlobalSettings(notificationsData.data)
            NotificationCreationMode.BOOSTERS -> renderBoosterSettings(notificationsData.data)
            NotificationCreationMode.DUNGEONS -> renderDungeonSettings(notificationsData.data)
            NotificationCreationMode.ITEM_DROP_LIST -> renderItemDropList(notificationsData.data)
            NotificationCreationMode.ITEM_DROP_DELETE -> renderItemDropDeleteConfirmation(notificationsData.data)
            NotificationCreationMode.ITEM_DROP -> renderItemDropCreation(notificationsData.data)
            NotificationCreationMode.RAID -> renderRaidEditor(notificationsData.data)
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
        val activeRaidLocations = activeRaidLocationCount(result)
        val dropRules = result.page.totalItems
        val pingState = if (result.pingEnabled) "Вкл" else "Выкл"

        return messageWithRows(
            text =
                listOfNotNull(
                    "🔔 <b>Уведомления</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(data.feedback),
                    "",
                    "🌌 Глобальные события: <b>$activeGlobals/${NotificationGlobalType.entries.size}</b>",
                    "⚡ Бустеры: <b>$activeBoosters/${NotificationBoosterType.entries.size}</b>",
                    "🏰 Dungeon: <b>$activeDungeons/${NotificationDungeonType.entries.size}</b>",
                    "🛡 RAID: <b>$activeRaidLocations/${NotificationRaidLocations.all.size}</b>",
                    "📦 Дропы: <b>$dropRules</b>",
                    "🔔 Упоминания: <b>$pingState</b>",
                ).joinToString("\n"),
            rows = dashboardRows(result),
        )
    }

    private fun dashboardRows(result: NotificationSubscriptionResult.Available): List<List<OutboundTelegramMessageAction>> {
        val globals = result.globalSubscriptions.size
        val boosters = result.boosterSubscriptions.size
        val dungeons = result.dungeonSubscriptions.size
        val raidLocations = activeRaidLocationCount(result)

        return listOf(
            listOf(
                categoryAction(
                    label = "🌌 Глобальные · $globals/${NotificationGlobalType.entries.size}",
                    data = NotificationCallbackData.GLOBALS,
                    active = globals > 0,
                ),
                categoryAction(
                    label = "⚡ Бустеры · $boosters/${NotificationBoosterType.entries.size}",
                    data = NotificationCallbackData.BOOSTERS,
                    active = boosters > 0,
                ),
            ),
            listOf(
                categoryAction(
                    label = "🏰 Dungeon · $dungeons/${NotificationDungeonType.entries.size}",
                    data = NotificationCallbackData.DUNGEONS,
                    active = dungeons > 0,
                ),
                categoryAction(
                    label = "🛡 RAID · $raidLocations/${NotificationRaidLocations.all.size}",
                    data = NotificationCallbackData.RAID_EDIT,
                    active = result.raidSubscription != null,
                ),
            ),
            listOf(
                callback(
                    label = "📦 Дропы · ${result.page.totalItems}",
                    data = NotificationCallbackData.DROPS,
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
                targetToggleCallback(
                    label = "🔔 Упоминания",
                    active = result.pingEnabled,
                    targetData = NotificationCallbackData::ping,
                ),
            ),
            listOf(
                callback(
                    label = "🎨 Паки уведомлений",
                    data = TelegramMenuScreen.PACKS.callbackData,
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
            ),
            listOf(refreshAction),
            listOf(backAction),
        )
    }

    private fun renderGlobalSettings(data: NotificationMenuScreenData): OutboundTelegramMessage =
        renderSimpleSettings(
            data = data,
            title = "🌌 <b>Глобальные события</b>",
            total = NotificationGlobalType.entries.size,
            activeCount = { result -> result.globalSubscriptions.size },
            rows = { result ->
                val actions =
                    listOf(
                        globalToggle(result, NotificationGlobalType.DARKNESS, "🌑 Тьма"),
                        globalToggle(result, NotificationGlobalType.MOON, "🌙 Луна"),
                        globalToggle(result, NotificationGlobalType.ECLIPSE, "🌘 Затмение"),
                        globalToggle(result, NotificationGlobalType.EXPLOSION, "💥 Взрыв"),
                        globalToggle(result, NotificationGlobalType.COMET_CHAOS, "☄️ Хаос"),
                    )

                listOf(
                    actions.take(3),
                    actions.drop(3),
                    listOf(refreshAction),
                    listOf(notificationsAction),
                )
            },
        )

    private fun renderBoosterSettings(data: NotificationMenuScreenData): OutboundTelegramMessage =
        renderSimpleSettings(
            data = data,
            title = "⚡ <b>Бустеры</b>",
            total = NotificationBoosterType.entries.size,
            activeCount = { result -> result.boosterSubscriptions.size },
            rows = { result ->
                val actions =
                    listOf(
                        boosterToggle(result, NotificationBoosterType.LUCK, "🍀 Удача"),
                        boosterToggle(result, NotificationBoosterType.MONEY, "💰 Деньги"),
                        boosterToggle(result, NotificationBoosterType.DAMAGE, "⚔️ Урон"),
                        boosterToggle(result, NotificationBoosterType.POWER, "💪 Сила"),
                    )

                actions.chunked(2) +
                    listOf(
                        listOf(refreshAction),
                        listOf(notificationsAction),
                    )
            },
        )

    private fun renderDungeonSettings(data: NotificationMenuScreenData): OutboundTelegramMessage =
        renderSimpleSettings(
            data = data,
            title = "🏰 <b>Dungeon</b>",
            total = NotificationDungeonType.entries.size,
            activeCount = { result -> result.dungeonSubscriptions.size },
            rows = { result ->
                val actions =
                    listOf(
                        dungeonToggle(result, NotificationDungeonType.EASY, "🟢 Легкий"),
                        dungeonToggle(result, NotificationDungeonType.MEDIUM, "🟡 Средний"),
                        dungeonToggle(result, NotificationDungeonType.HARD, "🔴 Сложный"),
                        dungeonToggle(result, NotificationDungeonType.EXPERT, "🟣 Эксперт"),
                    )

                actions.chunked(2) +
                    listOf(
                        listOf(refreshAction),
                        listOf(notificationsAction),
                    )
            },
        )

    private fun renderSimpleSettings(
        data: NotificationMenuScreenData,
        title: String,
        total: Int,
        activeCount: (NotificationSubscriptionResult.Available) -> Int,
        rows: (NotificationSubscriptionResult.Available) -> List<List<OutboundTelegramMessageAction>>,
    ): OutboundTelegramMessage =
        when (val result = data.result) {
            is NotificationSubscriptionResult.Available ->
                messageWithRows(
                    text =
                        listOfNotNull(
                            title,
                            MESSAGE_DIVIDER,
                            feedbackText(data.feedback),
                            "",
                            "Включено: <b>${activeCount(result)}/$total</b>",
                        ).joinToString("\n"),
                    rows = rows(result),
                )

            NotificationSubscriptionResult.Unavailable -> unavailableMessage(data.feedback)
            NotificationSubscriptionResult.NotLoaded -> unavailableMessage(data.feedback)
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

    private fun renderItemDropDeleteConfirmation(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val available =
            data.result as? NotificationSubscriptionResult.Available
                ?: return unavailableMessage(data.feedback)
        val subscriptionId = data.deleteSubscriptionId
        val subscription =
            subscriptionId?.let { selectedId ->
                available.page.items.firstOrNull { item -> item.id == selectedId }
            }

        if (subscriptionId == null || subscription == null) {
            return messageWithRows(
                text =
                    listOf(
                        "🗑 <b>Удалить правило?</b>",
                        MESSAGE_DIVIDER,
                        "",
                        "Правило уже недоступно.",
                    ).joinToString("\n"),
                rows = listOf(listOf(deleteCancelAction)),
            )
        }

        return messageWithRows(
            text =
                listOf(
                    "🗑 <b>Удалить правило?</b>",
                    MESSAGE_DIVIDER,
                    "",
                    renderItemDrop(subscription),
                    "",
                    "Это действие нельзя отменить.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(
                        callback(
                            label = "🗑 Удалить",
                            data = NotificationCallbackData.confirmDelete(subscriptionId),
                            style = OutboundTelegramButtonStyle.DANGER,
                            accented = true,
                        ),
                    ),
                    listOf(deleteCancelAction),
                ),
        )
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
                    "Правило срабатывает, когда совпадают все указанные фильтры.",
                    "Только игрок — любой его дроп. Только предмет — этот предмет у любого игрока.",
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

    private fun renderRaidEditor(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val available =
            data.result as? NotificationSubscriptionResult.Available
                ?: return unavailableMessage(data.feedback)
        val activeLocations = data.raidLocations
        val locationSummary =
            when {
                activeLocations.isEmpty() -> "Выкл"
                activeLocations.size == NotificationRaidLocations.all.size -> "Все"
                else -> activeLocations.sorted().joinToString(", ")
            }
        val firstLocation = data.raidPage * RAID_LOCATIONS_PER_PAGE + 1
        val lastLocation =
            (firstLocation + RAID_LOCATIONS_PER_PAGE - 1)
                .coerceAtMost(NotificationRaidLocations.MAX)
        val locationActions =
            (firstLocation..lastLocation).map { location ->
                compactToggleCallback(
                    label = location.toString(),
                    active = location in activeLocations,
                    targetData = { selected -> NotificationCallbackData.raidLocation(location, selected) },
                )
            }
        val pageActions =
            when (data.raidPage) {
                0 -> listOf(callback("2/2 ▶", NotificationCallbackData.raidPage(1)))
                else -> listOf(callback("◀ 1/2", NotificationCallbackData.raidPage(0)))
            }

        return messageWithRows(
            text =
                listOfNotNull(
                    "🛡 <b>Рейды</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(data.feedback),
                    "",
                    "📍 RAID: <b>${activeLocations.size}/${NotificationRaidLocations.all.size}</b>",
                    "└ ${escapeHtml(locationSummary)}",
                    "",
                    "Изменения применяются сразу.",
                    "Нажмите номер локации, чтобы включить или выключить её.",
                ).joinToString("\n"),
            rows =
                buildList {
                    addAll(locationActions.chunked(RAID_LOCATION_BUTTONS_PER_ROW))
                    add(pageActions)

                    if (available.raidSubscription != null) {
                        add(listOf(disableRaidAction))
                    }

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

    private fun activeRaidLocationCount(result: NotificationSubscriptionResult.Available): Int {
        val raid = result.raidSubscription ?: return 0

        return if (raid.raidLocations.isEmpty()) {
            NotificationRaidLocations.all.size
        } else {
            raid.raidLocations.size
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
            NotificationMenuFeedback.RAID_SAVED -> "✅ RAID · Настройка сохранена."
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
        accented: Boolean = false,
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
            style = style,
            accented = accented,
        )

    private fun categoryAction(
        label: String,
        data: String,
        active: Boolean,
    ): OutboundTelegramMessageAction.Callback =
        callback(
            label = label,
            data = data,
            style = activeStyle(active),
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

    private val refreshAction = callback("🔄 Обновить", NotificationCallbackData.REFRESH)
    private val backAction = callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA)
    private val notificationsAction = callback("← Уведомления", TelegramMenuScreen.NOTIFICATIONS.callbackData)
    private val dropsAction = callback("← Дропы", NotificationCallbackData.DROPS)
    private val deleteCancelAction = callback("← Не удалять", NotificationCallbackData.DELETE_CANCEL)
    private val disableRaidAction =
        callback(
            label = "✖ Отключить RAID",
            data = NotificationCallbackData.RAID_DISABLE,
            style = OutboundTelegramButtonStyle.DANGER,
        )
}
