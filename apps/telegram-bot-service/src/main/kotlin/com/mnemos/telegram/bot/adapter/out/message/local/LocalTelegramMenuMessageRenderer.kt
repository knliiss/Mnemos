package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.online.SaoOnlineCallbackData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class LocalTelegramMenuMessageRenderer : TelegramMenuMessageRenderer {
    override fun render(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
    ): OutboundTelegramMessage =
        when (screen) {
            TelegramMenuScreen.EVENTS -> EventHistoryMenuMessageRenderer.render(data)
            else ->
                OutboundTelegramMessage(
                    text = renderText(screen, data),
                    parseMode = OutboundTelegramMessageParseMode.HTML,
                    actions = renderActions(screen, data),
                )
        }

    private fun renderText(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
    ): String =
        when (screen) {
            TelegramMenuScreen.HOME ->
                """
                🧠 <b>Mnemos</b>

                Основные функции Mnemos доступны прямо в Telegram. Выберите раздел ниже.
                """.trimIndent()

            TelegramMenuScreen.NOTIFICATIONS ->
                """
                🔔 <b>Уведомления</b>

                Подписки на глобальные события, бустеры, данжи, рейды, дропы предметов и игроков.
                """.trimIndent()

            TelegramMenuScreen.ONLINE -> renderSaoOnline(data)

            TelegramMenuScreen.EVENTS ->
                """
                🕘 <b>История событий</b>

                Лента событий SAO с поиском, фильтрами и переходом к подпискам.
                """.trimIndent()

            TelegramMenuScreen.DAILY ->
                """
                🎁 <b>Daily</b>

                Ежедневная награда и текущая серия активного аккаунта.
                """.trimIndent()

            TelegramMenuScreen.ACCOUNT ->
                """
                👤 <b>Аккаунт</b>

                Активный Cristalix-аккаунт, баланс и переключение между аккаунтами.
                """.trimIndent()

            TelegramMenuScreen.SETTINGS ->
                """
                ⚙️ <b>Настройки</b>

                Язык, настройки Telegram-клиента и активный пак уведомлений.
                """.trimIndent()
        }

    private fun renderSaoOnline(data: TelegramMenuScreenData): String {
        val onlineData =
            data as? TelegramMenuScreenData.SaoOnline
                ?: return renderSaoOnlineUnavailable()

        return when (val screenData = onlineData.data) {
            is SaoOnlineMenuScreenData.RealmList -> renderSaoRealmList(screenData.snapshot)
            is SaoOnlineMenuScreenData.RealmDetail -> renderSaoRealmDetail(screenData)
            is SaoOnlineMenuScreenData.SearchPrompt -> renderSearchPrompt(screenData)
            is SaoOnlineMenuScreenData.SearchResults -> renderSearchResults(screenData)
            SaoOnlineMenuScreenData.Unavailable -> renderSaoOnlineUnavailable()
        }
    }

    private fun renderSaoRealmList(snapshot: SaoOnlineSnapshot): String {
        val realmLines =
            snapshot.realms
                .joinToString("\n") { realm ->
                    "${escapeHtml(realm.name)} — <b>${realm.playerCount}</b>"
                }.ifBlank {
                    "Нет активных реалмов."
                }
        val generatedAt = onlineTimestampFormatter.format(snapshot.generatedAt)

        return listOf(
            "🌐 <b>Онлайн SAO</b>",
            "",
            "Сейчас отслеживается: <b>${snapshot.trackedOnline}</b>",
            "",
            realmLines,
            "",
            "Обновлено: <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSaoRealmDetail(data: SaoOnlineMenuScreenData.RealmDetail): String {
        val firstPlayerNumber = data.page * ITEMS_PER_PAGE + 1
        val playerLines =
            data.players
                .mapIndexed { index, player ->
                    "${firstPlayerNumber + index}. <code>${escapeHtml(player.nickname)}</code>"
                }.joinToString("\n")
                .ifBlank {
                    "Сейчас на этом реалме никого нет."
                }
        val generatedAt = onlineTimestampFormatter.format(data.generatedAt)

        return listOf(
            "🌐 <b>${escapeHtml(data.realmName)}</b>",
            "",
            "Онлайн: <b>${data.playerCount}</b>",
            "Страница: <b>${data.page + 1}/${data.totalPages}</b>",
            "",
            playerLines,
            "",
            "Обновлено: <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSearchPrompt(data: SaoOnlineMenuScreenData.SearchPrompt): String {
        val lines =
            mutableListOf(
                "🔎 <b>Поиск игрока</b>",
                "",
                "Отправьте никнейм или его часть одним сообщением.",
                "От 2 до 32 символов, без пробелов.",
            )

        data.error?.let { error ->
            lines += ""
            lines += "⚠️ ${escapeHtml(error)}"
        }

        return lines.joinToString("\n")
    }

    private fun renderSearchResults(data: SaoOnlineMenuScreenData.SearchResults): String {
        val firstMatchNumber = data.page * ITEMS_PER_PAGE + 1
        val matchLines =
            data.matches
                .mapIndexed { index, match ->
                    val nickname = escapeHtml(match.nickname)
                    val realmName = escapeHtml(match.realmName)

                    "${firstMatchNumber + index}. <code>$nickname</code> — $realmName"
                }.joinToString("\n")
                .ifBlank {
                    "Онлайн-игроки по этому запросу не найдены."
                }
        val generatedAt = onlineTimestampFormatter.format(data.generatedAt)

        return listOf(
            "🔎 <b>Поиск игрока</b>",
            "",
            "Запрос: <code>${escapeHtml(data.query)}</code>",
            "Найдено: <b>${data.totalMatches}</b>",
            "Страница: <b>${data.page + 1}/${data.totalPages}</b>",
            "",
            matchLines,
            "",
            "Обновлено: <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSaoOnlineUnavailable(): String =
        listOf(
            "🌐 <b>Онлайн SAO</b>",
            "",
            "Не удалось получить актуальный онлайн. Попробуйте обновить через несколько секунд.",
        ).joinToString("\n")

    private fun renderActions(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
    ): List<OutboundTelegramMessageAction> =
        when (screen) {
            TelegramMenuScreen.HOME -> homeActions
            TelegramMenuScreen.ONLINE -> renderSaoOnlineActions(data)
            else -> navigationActions
        }

    private fun renderSaoOnlineActions(data: TelegramMenuScreenData): List<OutboundTelegramMessageAction> {
        val onlineData =
            data as? TelegramMenuScreenData.SaoOnline
                ?: return searchAndRefreshActions + navigationActions

        return when (val screenData = onlineData.data) {
            is SaoOnlineMenuScreenData.RealmList -> renderRealmListActions(screenData.snapshot)
            is SaoOnlineMenuScreenData.RealmDetail -> renderRealmDetailActions(screenData)
            is SaoOnlineMenuScreenData.SearchPrompt -> searchPromptActions
            is SaoOnlineMenuScreenData.SearchResults -> renderSearchResultsActions(screenData)
            SaoOnlineMenuScreenData.Unavailable -> searchAndRefreshActions + navigationActions
        }
    }

    private fun renderRealmListActions(snapshot: SaoOnlineSnapshot): List<OutboundTelegramMessageAction> {
        val realmActions =
            snapshot.realms.map { realm ->
                OutboundTelegramMessageAction.Callback(
                    label = "${realm.name} · ${realm.playerCount}",
                    data = SaoOnlineCallbackData.realm(realm.number),
                )
            }

        return searchAndRefreshActions + realmActions + navigationActions
    }

    private fun renderRealmDetailActions(data: SaoOnlineMenuScreenData.RealmDetail): List<OutboundTelegramMessageAction> =
        searchAndRefreshActions + pageActions(data.page, data.totalPages) + realmListAction + navigationActions

    private fun renderSearchResultsActions(data: SaoOnlineMenuScreenData.SearchResults): List<OutboundTelegramMessageAction> =
        searchAndRefreshActions + pageActions(data.page, data.totalPages) + realmListAction + navigationActions

    private fun pageActions(
        page: Int,
        totalPages: Int,
    ): List<OutboundTelegramMessageAction> {
        val actions = mutableListOf<OutboundTelegramMessageAction>()

        if (page > 0) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "← Страница",
                    data = SaoOnlineCallbackData.PREVIOUS_PAGE,
                )
        }

        if (page + 1 < totalPages) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "Страница →",
                    data = SaoOnlineCallbackData.NEXT_PAGE,
                )
        }

        return actions
    }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private companion object {
        const val ITEMS_PER_PAGE = 10

        val searchAction =
            OutboundTelegramMessageAction.Callback(
                "🔎 Найти игрока",
                SaoOnlineCallbackData.SEARCH,
            )

        val refreshAction =
            OutboundTelegramMessageAction.Callback(
                "🔄 Обновить",
                SaoOnlineCallbackData.REFRESH,
            )

        val searchAndRefreshActions = listOf(searchAction, refreshAction)

        val realmListAction =
            OutboundTelegramMessageAction.Callback(
                "← К реалмам",
                SaoOnlineCallbackData.REALM_LIST,
            )

        val searchPromptActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    "✖ Отмена",
                    SaoOnlineCallbackData.SEARCH_CANCEL,
                ),
                OutboundTelegramMessageAction.Callback(
                    "⌂ Меню",
                    TelegramMenuScreen.HOME.callbackData,
                ),
                OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
            )

        val navigationActions =
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

        val homeActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    "🔔 Уведомления",
                    TelegramMenuScreen.NOTIFICATIONS.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🌐 Онлайн",
                    TelegramMenuScreen.ONLINE.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🕘 История",
                    TelegramMenuScreen.EVENTS.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🎁 Daily",
                    TelegramMenuScreen.DAILY.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "👤 Аккаунт",
                    TelegramMenuScreen.ACCOUNT.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "⚙️ Настройки",
                    TelegramMenuScreen.SETTINGS.callbackData,
                ),
                OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
            )

        val onlineTimestampFormatter: DateTimeFormatter =
            DateTimeFormatter
                .ofPattern("dd.MM HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC)
    }
}
