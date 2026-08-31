package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
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
            NotificationCreationMode.TYPE_SELECTION -> renderTypeSelection(notificationsData.data)
            NotificationCreationMode.ITEM_DROP -> renderItemDropCreation(notificationsData.data)
            NotificationCreationMode.BOOSTER -> renderBoosterCreation(notificationsData.data)
            NotificationCreationMode.DUNGEON -> renderDungeonCreation(notificationsData.data)
            NotificationCreationMode.RAID -> renderRaidCreation(notificationsData.data)
            NotificationCreationMode.GLOBAL -> renderGlobalCreation(notificationsData.data)
        }
    }

    private fun renderList(data: NotificationMenuScreenData): OutboundTelegramMessage =
        when (val result = data.result) {
            is NotificationSubscriptionResult.Available -> availableMessage(data, result)
            NotificationSubscriptionResult.Unavailable -> unavailableMessage(data.feedback)
            NotificationSubscriptionResult.NotLoaded -> unavailableMessage(data.feedback)
        }

    private fun availableMessage(
        data: NotificationMenuScreenData,
        result: NotificationSubscriptionResult.Available,
    ): OutboundTelegramMessage {
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
            listOfNotNull(
                "🔔 <b>Уведомления</b>",
                feedbackText(data.feedback),
                "Активных подписок: <b>${page.totalItems}</b>",
                pageLine,
                "",
                subscriptionLines,
                deleteHint(page.items),
            ).filter(String::isNotBlank)
                .joinToString("\n")

        return message(
            text = text,
            actions = renderListActions(page.items, page.page, page.hasNextPage),
        )
    }

    private fun renderTypeSelection(data: NotificationMenuScreenData): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "➕ <b>Новая подписка</b>",
                    feedbackText(data.feedback),
                    "",
                    "Выберите тип события, о котором хотите получать уведомления.",
                ).joinToString("\n"),
            actions =
                listOf(
                    callback("⚔️ Дроп", NotificationCallbackData.type(NotificationSubscriptionType.ITEM_DROP)),
                    callback("⚡ Бустер", NotificationCallbackData.type(NotificationSubscriptionType.BOOSTER)),
                    callback("🏰 Данж", NotificationCallbackData.type(NotificationSubscriptionType.DUNGEON)),
                    callback("🛡 Рейд", NotificationCallbackData.type(NotificationSubscriptionType.RAID)),
                    callback("🌒 Глобальное", NotificationCallbackData.type(NotificationSubscriptionType.GLOBAL)),
                ) + listNavigationActions,
        )

    private fun renderItemDropCreation(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val playerFilter = data.droppedFor?.let { value -> "<code>${escapeHtml(value)}</code>" } ?: "—"
        val itemFilter = data.itemName?.let { value -> "<code>${escapeHtml(value)}</code>" } ?: "—"
        val inputPrompt =
            when (data.itemDropInput) {
                NotificationItemDropInputField.PLAYER ->
                    "Отправьте ник игрока: 4–20 символов, буквы, цифры или <code>_</code>."

                NotificationItemDropInputField.ITEM ->
                    "Отправьте название предмета: от 1 до 24 символов."

                null -> null
            }
        val text =
            listOfNotNull(
                "⚔️ <b>Подписка на дроп</b>",
                feedbackText(data.feedback),
                "",
                "Игрок: $playerFilter",
                "Предмет: $itemFilter",
                "",
                inputPrompt ?: "Укажите хотя бы один фильтр. Можно использовать оба одновременно.",
            ).joinToString("\n")

        return message(
            text = text,
            actions = renderItemDropActions(data),
        )
    }

    private fun renderItemDropActions(data: NotificationMenuScreenData): List<OutboundTelegramMessageAction> {
        if (data.itemDropInput != null) {
            return listOf(
                callback("✖ Отмена ввода", NotificationCallbackData.ITEM_DROP_INPUT_CANCEL),
                homeAction,
            )
        }

        val actions =
            mutableListOf<OutboundTelegramMessageAction>(
                callback("👤 Игрок", NotificationCallbackData.ITEM_DROP_PLAYER),
                callback("⚔️ Предмет", NotificationCallbackData.ITEM_DROP_ITEM),
            )

        if (data.droppedFor != null) {
            actions += callback("✖ Игрок", NotificationCallbackData.ITEM_DROP_CLEAR_PLAYER)
        }

        if (data.itemName != null) {
            actions += callback("✖ Предмет", NotificationCallbackData.ITEM_DROP_CLEAR_ITEM)
        }

        if (data.droppedFor != null || data.itemName != null) {
            actions += callback("✓ Создать", NotificationCallbackData.ITEM_DROP_CREATE)
        }

        actions += creationNavigationActions

        return actions
    }

    private fun renderBoosterCreation(data: NotificationMenuScreenData): OutboundTelegramMessage =
        choiceMessage(
            title = "⚡ <b>Подписка на бустер</b>",
            description = "Выберите тип бустера.",
            feedback = data.feedback,
            actions =
                listOf(
                    callback("🍀 Удача", NotificationCallbackData.booster(NotificationBoosterType.LUCK)),
                    callback("💰 Деньги", NotificationCallbackData.booster(NotificationBoosterType.MONEY)),
                    callback("⚔️ Урон", NotificationCallbackData.booster(NotificationBoosterType.DAMAGE)),
                    callback("💪 Сила", NotificationCallbackData.booster(NotificationBoosterType.POWER)),
                ),
        )

    private fun renderDungeonCreation(data: NotificationMenuScreenData): OutboundTelegramMessage =
        choiceMessage(
            title = "🏰 <b>Подписка на данж</b>",
            description = "Выберите сложность данжа.",
            feedback = data.feedback,
            actions =
                listOf(
                    callback("Легкий", NotificationCallbackData.dungeon(NotificationDungeonType.EASY)),
                    callback("Средний", NotificationCallbackData.dungeon(NotificationDungeonType.MEDIUM)),
                    callback("Сложный", NotificationCallbackData.dungeon(NotificationDungeonType.HARD)),
                    callback("Эксперт", NotificationCallbackData.dungeon(NotificationDungeonType.EXPERT)),
                ),
        )

    private fun renderGlobalCreation(data: NotificationMenuScreenData): OutboundTelegramMessage =
        choiceMessage(
            title = "🌒 <b>Подписка на глобальное событие</b>",
            description = "Выберите событие.",
            feedback = data.feedback,
            actions =
                listOf(
                    callback("🌑 Тьма", NotificationCallbackData.global(NotificationGlobalType.DARKNESS)),
                    callback("🌙 Луна", NotificationCallbackData.global(NotificationGlobalType.MOON)),
                    callback("🌘 Затмение", NotificationCallbackData.global(NotificationGlobalType.ECLIPSE)),
                    callback("💥 Взрыв", NotificationCallbackData.global(NotificationGlobalType.EXPLOSION)),
                    callback("☄️ Хаос кометы", NotificationCallbackData.global(NotificationGlobalType.COMET_CHAOS)),
                ),
        )

    private fun renderRaidCreation(data: NotificationMenuScreenData): OutboundTelegramMessage {
        val selectedLocations =
            if (data.raidLocations.isEmpty()) {
                "любая локация"
            } else {
                data.raidLocations.sorted().joinToString(", ")
            }
        val firstLocation = data.raidPage * RAID_LOCATIONS_PER_PAGE + 1
        val lastLocation = (firstLocation + RAID_LOCATIONS_PER_PAGE - 1).coerceAtMost(MAX_RAID_LOCATION)
        val locationActions =
            (firstLocation..lastLocation).map { location ->
                val selectedPrefix = if (location in data.raidLocations) "✓ " else ""

                callback(
                    label = "$selectedPrefix$location",
                    data = NotificationCallbackData.raidLocation(location),
                )
            }
        val pageActions =
            when (data.raidPage) {
                0 -> listOf(callback("15–28 →", NotificationCallbackData.raidPage(1)))
                else -> listOf(callback("← 1–14", NotificationCallbackData.raidPage(0)))
            }
        val actions =
            locationActions +
                pageActions +
                listOf(
                    callback("◎ Любая локация", NotificationCallbackData.RAID_CLEAR),
                    callback("✓ Создать", NotificationCallbackData.RAID_CREATE),
                ) +
                creationNavigationActions

        return message(
            text =
                listOfNotNull(
                    "🛡 <b>Подписка на рейд</b>",
                    feedbackText(data.feedback),
                    "",
                    "Локации: <b>${escapeHtml(selectedLocations)}</b>",
                    "",
                    "Если ничего не выбрано, уведомления будут приходить для рейда на любой локации.",
                ).joinToString("\n"),
            actions = actions,
        )
    }

    private fun choiceMessage(
        title: String,
        description: String,
        feedback: NotificationMenuFeedback?,
        actions: List<OutboundTelegramMessageAction>,
    ): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    title,
                    feedbackText(feedback),
                    "",
                    description,
                ).joinToString("\n"),
            actions = actions + creationNavigationActions,
        )

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

    private fun renderListActions(
        items: List<NotificationSubscription>,
        page: Int,
        hasNextPage: Boolean,
    ): List<OutboundTelegramMessageAction> {
        val actions =
            mutableListOf<OutboundTelegramMessageAction>(
                callback("➕ Добавить подписку", NotificationCallbackData.CREATE),
            )

        items.forEachIndexed { index, subscription ->
            actions +=
                callback(
                    label = "✖ ${index + 1}",
                    data = NotificationCallbackData.delete(subscription.id),
                )
        }

        if (page > 0) {
            actions += callback("← Страница", NotificationCallbackData.page(page - 1))
        }

        if (hasNextPage) {
            actions += callback("Страница →", NotificationCallbackData.page(page + 1))
        }

        actions += refreshAction
        actions += navigationActions

        return actions
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

    private fun displayValue(value: String?): String =
        value
            ?.takeIf(String::isNotBlank)
            ?.lowercase()
            ?.replace('_', ' ')
            ?.replaceFirstChar { character -> character.titlecase() }
            ?.let(::escapeHtml)
            ?: "—"

    private fun unavailableMessage(feedback: NotificationMenuFeedback? = null): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "🔔 <b>Уведомления</b>",
                    feedbackText(feedback),
                    "",
                    "Не удалось получить подписки. Попробуйте обновить через несколько секунд.",
                ).joinToString("\n"),
            actions = listOf(createAction, refreshAction) + navigationActions,
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
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private const val RAID_LOCATIONS_PER_PAGE = 14
    private const val MAX_RAID_LOCATION = 28

    private val createAction = callback("➕ Добавить подписку", NotificationCallbackData.CREATE)
    private val refreshAction = callback("🔄 Обновить", NotificationCallbackData.REFRESH)
    private val homeAction = callback("⌂ Меню", TelegramMenuScreen.HOME.callbackData)

    private val listNavigationActions =
        listOf(
            callback("← Подписки", TelegramMenuScreen.NOTIFICATIONS.callbackData),
            homeAction,
        )

    private val creationNavigationActions =
        listOf(
            callback("← Типы", NotificationCallbackData.CREATE_TYPES),
            callback("← Подписки", TelegramMenuScreen.NOTIFICATIONS.callbackData),
            homeAction,
        )

    private val navigationActions =
        listOf(
            callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA),
            homeAction,
        )
}
