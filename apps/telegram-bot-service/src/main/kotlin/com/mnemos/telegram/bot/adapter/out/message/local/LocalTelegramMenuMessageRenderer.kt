package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.online.SaoOnlineCallbackData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class LocalTelegramMenuMessageRenderer : TelegramMenuMessageRenderer {
    override fun render(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage {
        val rawMessage =
            when (screen) {
                TelegramMenuScreen.HOME -> homeMessage()
                TelegramMenuScreen.MORE -> homeMessage()
                TelegramMenuScreen.NOTIFICATIONS -> NotificationMenuMessageRenderer.render(data)
                TelegramMenuScreen.ONLINE -> onlineMessage(data)
                TelegramMenuScreen.EVENTS -> EventHistoryMenuMessageRenderer.render(data)
                TelegramMenuScreen.DAILY -> DailyMenuMessageRenderer.render(data)
                TelegramMenuScreen.ACCOUNT -> AccountMenuMessageRenderer.render(data)
                TelegramMenuScreen.BETS -> BetMenuMessageRenderer.render(data)
                TelegramMenuScreen.PRIME -> PrimeMenuMessageRenderer.render(data)
                TelegramMenuScreen.SETTINGS -> SettingsMenuMessageRenderer.render(data)
            }

        return rawMessage
            .localized(language)
            .withStructuredKeyboard()
    }

    private fun homeMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                """
                🤖 <b>Mnemos</b>
                $MESSAGE_DIVIDER

                📋 <b>Панель управления</b>
                Выберите раздел:
                """.trimIndent(),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actionRows = homeActionRows,
        )

    private fun onlineMessage(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val rows = renderSaoOnlineRows(data)

        return OutboundTelegramMessage(
            text = renderSaoOnline(data),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
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
                    "├ ${escapeHtml(realm.name)}: <b>${realm.playerCount}</b>"
                }.ifBlank {
                    "└ Нет активных реалмов."
                }
        val generatedAt = onlineTimestampFormatter.format(snapshot.generatedAt)

        return listOf(
            "🌐 <b>SAO и события</b>",
            MESSAGE_DIVIDER,
            "",
            "👥 <b>Онлайн</b>",
            "└ Всего: <b>${snapshot.trackedOnline}</b>",
            "",
            "🗺 <b>Реалмы</b>",
            realmLines,
            "",
            "🕒 <code>$generatedAt</code>",
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
        val totalPages = data.totalPages.coerceAtLeast(1)

        return listOf(
            "🌐 <b>${escapeHtml(data.realmName)}</b>",
            MESSAGE_DIVIDER,
            "",
            "👥 Онлайн: <b>${data.playerCount}</b>",
            "📄 Страница: <b>${data.page + 1}/$totalPages</b>",
            "",
            playerLines,
            "",
            "🕒 <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSearchPrompt(data: SaoOnlineMenuScreenData.SearchPrompt): String {
        val lines =
            mutableListOf(
                "🔎 <b>Поиск игрока</b>",
                MESSAGE_DIVIDER,
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
        val totalPages = data.totalPages.coerceAtLeast(1)

        return listOf(
            "🔎 <b>Поиск игрока</b>",
            MESSAGE_DIVIDER,
            "",
            "Запрос: <code>${escapeHtml(data.query)}</code>",
            "Найдено: <b>${data.totalMatches}</b>",
            "Страница: <b>${data.page + 1}/$totalPages</b>",
            "",
            matchLines,
            "",
            "🕒 <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSaoOnlineUnavailable(): String =
        listOf(
            "🌐 <b>SAO и события</b>",
            MESSAGE_DIVIDER,
            "",
            "⚠️ Не удалось получить актуальный онлайн.",
        ).joinToString("\n")

    private fun renderSaoOnlineRows(data: TelegramMenuScreenData): List<List<OutboundTelegramMessageAction>> {
        val onlineData =
            data as? TelegramMenuScreenData.SaoOnline
                ?: return unavailableOnlineRows

        return when (val screenData = onlineData.data) {
            is SaoOnlineMenuScreenData.RealmList -> renderRealmListRows(screenData.snapshot)
            is SaoOnlineMenuScreenData.RealmDetail -> renderRealmDetailRows(screenData)
            is SaoOnlineMenuScreenData.SearchPrompt -> listOf(searchPromptActions)
            is SaoOnlineMenuScreenData.SearchResults -> renderSearchResultsRows(screenData)
            SaoOnlineMenuScreenData.Unavailable -> unavailableOnlineRows
        }
    }

    private fun renderRealmListRows(snapshot: SaoOnlineSnapshot): List<List<OutboundTelegramMessageAction>> {
        val realmActions =
            snapshot.realms.map { realm ->
                OutboundTelegramMessageAction.Callback(
                    label = "🗺 ${realm.name} · ${realm.playerCount}",
                    data = SaoOnlineCallbackData.realm(realm.number),
                    style = OutboundTelegramButtonStyle.PRIMARY,
                )
            }

        return buildList {
            add(sectionRow)
            addAll(realmActions.adaptiveRows())
            add(listOf(refreshAction))
            add(listOf(backAction))
        }
    }

    private fun renderRealmDetailRows(data: SaoOnlineMenuScreenData.RealmDetail): List<List<OutboundTelegramMessageAction>> =
        buildList {
            add(sectionRow)

            val pagination = pageActions(data.page, data.totalPages)

            if (pagination.isNotEmpty()) {
                add(pagination)
            }

            add(listOf(realmListAction, refreshAction))
            add(listOf(backAction))
        }

    private fun renderSearchResultsRows(data: SaoOnlineMenuScreenData.SearchResults): List<List<OutboundTelegramMessageAction>> =
        buildList {
            add(listOf(eventHistoryAction))

            val pagination = pageActions(data.page, data.totalPages)

            if (pagination.isNotEmpty()) {
                add(pagination)
            }

            add(listOf(realmListAction, refreshAction))
            add(listOf(backAction))
        }

    private fun pageActions(
        page: Int,
        totalPages: Int,
    ): List<OutboundTelegramMessageAction> {
        val normalizedTotalPages = totalPages.coerceAtLeast(1)
        val actions = mutableListOf<OutboundTelegramMessageAction>()

        if (page > 0) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "◀ $page/$normalizedTotalPages",
                    data = SaoOnlineCallbackData.PREVIOUS_PAGE,
                )
        }

        if (page + 1 < normalizedTotalPages) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "${page + 2}/$normalizedTotalPages ▶",
                    data = SaoOnlineCallbackData.NEXT_PAGE,
                )
        }

        return actions
    }

    private fun OutboundTelegramMessage.localized(language: TelegramSettingsLanguage): OutboundTelegramMessage {
        if (language == TelegramSettingsLanguage.RU) {
            return this
        }

        return copy(
            text = translate(text, language),
            actions = actions.map { action -> action.localized(language) },
            actionRows =
                actionRows.map { row ->
                    row.map { action -> action.localized(language) }
                },
        )
    }

    private fun OutboundTelegramMessageAction.localized(language: TelegramSettingsLanguage): OutboundTelegramMessageAction =
        when (this) {
            is OutboundTelegramMessageAction.Callback ->
                copy(label = translate(label, language))

            is OutboundTelegramMessageAction.OpenMiniApp ->
                copy(label = translate(label, language))

            is OutboundTelegramMessageAction.OpenUrl ->
                copy(label = translate(label, language))
        }

    private fun translate(
        value: String,
        language: TelegramSettingsLanguage,
    ): String =
        BotMenuDesignTranslations.translate(
            value = BotMenuTranslations.translate(value, language),
            language = language,
        )

    private fun OutboundTelegramMessage.withStructuredKeyboard(): OutboundTelegramMessage {
        if (actionRows.isNotEmpty() || actions.isEmpty()) {
            return this
        }

        val normalizedActions = actions.withoutRedundantHomeAction()
        val standaloneRows = mutableListOf<List<OutboundTelegramMessageAction>>()
        val mainActions = mutableListOf<OutboundTelegramMessageAction>()
        val pageActions = mutableListOf<OutboundTelegramMessageAction>()
        val refreshActions = mutableListOf<OutboundTelegramMessageAction>()
        val navigationActions = mutableListOf<OutboundTelegramMessageAction>()

        normalizedActions.forEach { action ->
            when {
                action is OutboundTelegramMessageAction.OpenMiniApp ||
                    action is OutboundTelegramMessageAction.OpenUrl -> standaloneRows += listOf(action)

                action.isNavigationAction() -> navigationActions += action
                action.isPaginationAction() -> pageActions += action
                action.isRefreshAction() -> refreshActions += action
                else -> mainActions += action
            }
        }

        val rows =
            buildList {
                addAll(mainActions.adaptiveRows())

                if (pageActions.isNotEmpty()) {
                    add(pageActions)
                }

                if (refreshActions.isNotEmpty()) {
                    add(refreshActions)
                }

                addAll(standaloneRows)

                navigationActions.forEach { action ->
                    add(listOf(action))
                }
            }

        return copy(
            actions = emptyList(),
            actionRows = rows,
        )
    }

    private fun List<OutboundTelegramMessageAction>.withoutRedundantHomeAction(): List<OutboundTelegramMessageAction> {
        val hasBackAction =
            any { action ->
                action is OutboundTelegramMessageAction.Callback &&
                    (action.data == TelegramMenuScreen.BACK_CALLBACK_DATA || action.label.startsWith("←"))
            }

        if (!hasBackAction) {
            return this
        }

        return filterNot { action ->
            action is OutboundTelegramMessageAction.Callback &&
                action.data == TelegramMenuScreen.HOME.callbackData
        }
    }

    private fun OutboundTelegramMessageAction.isNavigationAction(): Boolean =
        this is OutboundTelegramMessageAction.Callback &&
            (data == TelegramMenuScreen.BACK_CALLBACK_DATA || data == TelegramMenuScreen.HOME.callbackData)

    private fun OutboundTelegramMessageAction.isPaginationAction(): Boolean =
        this is OutboundTelegramMessageAction.Callback &&
            (label.contains('◀') || label.contains('▶') || label.contains('←') || label.contains('→'))

    private fun OutboundTelegramMessageAction.isRefreshAction(): Boolean = label.startsWith("🔄")

    private fun List<OutboundTelegramMessageAction>.adaptiveRows(): List<List<OutboundTelegramMessageAction>> {
        if (isEmpty()) {
            return emptyList()
        }

        val result = mutableListOf<List<OutboundTelegramMessageAction>>()
        val buffer = mutableListOf<OutboundTelegramMessageAction>()

        fun flush() {
            if (buffer.isEmpty()) {
                return
            }

            val longestLabel = buffer.maxOf { action -> action.label.length }
            val size =
                when {
                    longestLabel <= 6 -> 5
                    longestLabel <= 12 -> 4
                    longestLabel <= 18 -> 3
                    else -> 1
                }

            result += buffer.chunked(size)
            buffer.clear()
        }

        forEach { action ->
            if (action is OutboundTelegramMessageAction.OpenMiniApp || action is OutboundTelegramMessageAction.OpenUrl) {
                flush()
                result += listOf(action)
            } else {
                buffer += action
            }
        }

        flush()

        return result
    }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private companion object {
        const val ITEMS_PER_PAGE = 10
        const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

        val searchAction =
            OutboundTelegramMessageAction.Callback(
                label = "🔎 Найти игрока",
                data = SaoOnlineCallbackData.SEARCH,
                style = OutboundTelegramButtonStyle.PRIMARY,
            )

        val eventHistoryAction =
            OutboundTelegramMessageAction.Callback(
                label = "📜 История событий",
                data = TelegramMenuScreen.EVENTS.callbackData,
                style = OutboundTelegramButtonStyle.PRIMARY,
            )

        val refreshAction =
            OutboundTelegramMessageAction.Callback(
                label = "🔄 Обновить",
                data = SaoOnlineCallbackData.REFRESH,
            )

        val sectionRow = listOf(searchAction, eventHistoryAction)

        val realmListAction =
            OutboundTelegramMessageAction.Callback(
                label = "← К реалмам",
                data = SaoOnlineCallbackData.REALM_LIST,
            )

        val searchPromptActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    label = "✖ Отмена",
                    data = SaoOnlineCallbackData.SEARCH_CANCEL,
                    style = OutboundTelegramButtonStyle.DANGER,
                ),
            )

        val backAction =
            OutboundTelegramMessageAction.Callback(
                label = "← Назад",
                data = TelegramMenuScreen.BACK_CALLBACK_DATA,
            )

        val unavailableOnlineRows =
            listOf(
                sectionRow,
                listOf(refreshAction),
                listOf(backAction),
            )

        val homeActionRows =
            listOf(
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "👤 Аккаунт",
                        data = TelegramMenuScreen.ACCOUNT.callbackData,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "🔔 Уведомления",
                        data = TelegramMenuScreen.NOTIFICATIONS.callbackData,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "🌐 SAO и события",
                        data = TelegramMenuScreen.ONLINE.callbackData,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "🎲 Ставки",
                        data = TelegramMenuScreen.BETS.callbackData,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "⚙️ Настройки",
                        data = TelegramMenuScreen.SETTINGS.callbackData,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
                listOf(
                    OutboundTelegramMessageAction.OpenMiniApp("↗️ Mini App"),
                ),
            )

        val onlineTimestampFormatter: DateTimeFormatter =
            DateTimeFormatter
                .ofPattern("dd.MM HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC)
    }
}
