package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.online.SaoOnlineCallbackData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayerSearchMatch
import com.mnemos.telegram.bot.application.online.model.SaoOnlineRealm
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LocalTelegramMenuMessageRendererTest {
    private val renderer = LocalTelegramMenuMessageRenderer()

    @Test
    fun `home menu separates online and event history`() {
        val message = renderer.render(TelegramMenuScreen.HOME, TelegramMenuScreenData.Empty, TelegramSettingsLanguage.RU)
        val callbacks = message.callbacks()
        val miniAppActions =
            message.actionRows
                .flatten()
                .filterIsInstance<OutboundTelegramMessageAction.OpenMiniApp>()

        assertTrue(message.text.contains("Панель управления"))
        assertEquals(listOf(1, 1, 1, 1, 1, 1, 1), message.actionRows.map(List<*>::size))
        assertEquals(1, miniAppActions.size)
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.ACCOUNT.callbackData })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.NOTIFICATIONS.callbackData })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.ONLINE.callbackData })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.EVENTS.callbackData })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BETS.callbackData })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.SETTINGS.callbackData })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.MORE.callbackData })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.DAILY.callbackData })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.PRIME.callbackData })
    }

    @Test
    fun `legacy more callback resolves to the same unified root menu`() {
        val message = renderer.render(TelegramMenuScreen.MORE, TelegramMenuScreenData.Empty, TelegramSettingsLanguage.RU)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Панель управления"))
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.ACCOUNT.callbackData })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.MORE.callbackData })
    }

    @Test
    fun `home menu localizes separate online and event history labels to english`() {
        val message = renderer.render(TelegramMenuScreen.HOME, TelegramMenuScreenData.Empty, TelegramSettingsLanguage.EN)
        val labels = message.actionRows.flatten().map(OutboundTelegramMessageAction::label)

        assertTrue(message.text.contains("Control panel"))
        assertTrue(labels.any { label -> label.contains("Account") })
        assertTrue(labels.any { label -> label.contains("Notifications") })
        assertTrue(labels.any { label -> label.contains("Online") })
        assertTrue(labels.any { label -> label.contains("Event history") })
        assertFalse(labels.any { label -> label.contains("More") })
    }

    @Test
    fun `realm list focuses only on online and realm browsing`() {
        val snapshot =
            SaoOnlineSnapshot(
                generatedAt = Instant.parse("2026-08-30T12:24:00Z"),
                trackedOnline = 58,
                realms =
                    listOf(
                        SaoOnlineRealm(
                            name = "SAO-1",
                            number = 1,
                            playerCount = 21,
                            players = emptyList(),
                        ),
                        SaoOnlineRealm(
                            name = "SAO-2",
                            number = 2,
                            playerCount = 37,
                            players = emptyList(),
                        ),
                    ),
            )
        val data = TelegramMenuScreenData.SaoOnline(SaoOnlineMenuScreenData.RealmList(snapshot))

        val message = renderer.render(TelegramMenuScreen.ONLINE, data, TelegramSettingsLanguage.RU)
        val callbacks = message.callbacks()
        val searchCallbacks = message.actionRows[0].filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val realmCallbacks = message.actionRows[1].filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Онлайн SAO"))
        assertTrue(message.text.contains("Всего онлайн: <b>58</b>"))
        assertTrue(message.text.contains("SAO-1: <b>21</b>"))
        assertTrue(message.text.contains("SAO-2: <b>37</b>"))
        assertTrue(message.text.contains("30.08 12:24 UTC"))
        assertEquals(listOf(1, 2, 1, 1), message.actionRows.map(List<*>::size))
        assertTrue(searchCallbacks.any { action -> action.data == SaoOnlineCallbackData.SEARCH })
        assertTrue(realmCallbacks.any { action -> action.data == "on:r:1" })
        assertTrue(realmCallbacks.any { action -> action.data == "on:r:2" })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.EVENTS.callbackData })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REFRESH })
    }

    @Test
    fun `realm detail keeps pagination and realm navigation without event history`() {
        val data =
            TelegramMenuScreenData.SaoOnline(
                SaoOnlineMenuScreenData.RealmDetail(
                    generatedAt = Instant.parse("2026-08-30T12:24:00Z"),
                    realmName = "SAO-4",
                    realmNumber = 4,
                    playerCount = 12,
                    page = 1,
                    totalPages = 2,
                    players =
                        listOf(
                            player("player11"),
                            player("name<12>"),
                        ),
                ),
            )

        val message = renderer.render(TelegramMenuScreen.ONLINE, data, TelegramSettingsLanguage.RU)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("<b>SAO-4</b>"))
        assertTrue(message.text.contains("Страница: <b>2/2</b>"))
        assertTrue(message.text.contains("11. <code>player11</code>"))
        assertTrue(message.text.contains("12. <code>name&lt;12&gt;</code>"))
        assertEquals(listOf(1, 1, 1, 1), message.actionRows.map(List<*>::size))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.PREVIOUS_PAGE })
        assertTrue(callbacks.none { action -> action.data == SaoOnlineCallbackData.NEXT_PAGE })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REALM_LIST })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REFRESH })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.EVENTS.callbackData })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
    }

    @Test
    fun `search prompt renders validation error and cancel only row`() {
        val data =
            TelegramMenuScreenData.SaoOnline(
                SaoOnlineMenuScreenData.SearchPrompt(
                    error = "invalid <query>",
                ),
            )

        val message = renderer.render(TelegramMenuScreen.ONLINE, data, TelegramSettingsLanguage.RU)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Отправьте никнейм или его часть"))
        assertTrue(message.text.contains("invalid &lt;query&gt;"))
        assertEquals(listOf(1), message.actionRows.map(List<*>::size))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.SEARCH_CANCEL })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.HOME.callbackData })
    }

    @Test
    fun `search results return to realms without event history shortcut`() {
        val data =
            TelegramMenuScreenData.SaoOnline(
                SaoOnlineMenuScreenData.SearchResults(
                    generatedAt = Instant.parse("2026-08-30T12:24:00Z"),
                    query = "name<",
                    totalMatches = 2,
                    page = 0,
                    totalPages = 1,
                    matches =
                        listOf(
                            SaoOnlinePlayerSearchMatch(
                                nickname = "name<one>",
                                realmName = "SAO-1",
                                realmNumber = 1,
                            ),
                            SaoOnlinePlayerSearchMatch(
                                nickname = "name<two>",
                                realmName = "SAO-2",
                                realmNumber = 2,
                            ),
                        ),
                ),
            )

        val message = renderer.render(TelegramMenuScreen.ONLINE, data, TelegramSettingsLanguage.RU)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Запрос: <code>name&lt;</code>"))
        assertTrue(message.text.contains("<code>name&lt;one&gt;</code> — SAO-1"))
        assertTrue(message.text.contains("Найдено: <b>2</b>"))
        assertEquals(listOf(1, 1, 1), message.actionRows.map(List<*>::size))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REALM_LIST })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.EVENTS.callbackData })
    }

    @Test
    fun `online unavailable state keeps search refresh and back only`() {
        val data = TelegramMenuScreenData.SaoOnline(SaoOnlineMenuScreenData.Unavailable)

        val message = renderer.render(TelegramMenuScreen.ONLINE, data, TelegramSettingsLanguage.RU)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Не удалось получить актуальный онлайн"))
        assertEquals(listOf(1, 1, 1), message.actionRows.map(List<*>::size))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.SEARCH })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.EVENTS.callbackData })
    }

    private fun OutboundTelegramMessage.callbacks(): List<OutboundTelegramMessageAction.Callback> {
        val source = actions.ifEmpty { actionRows.flatten() }

        return source.filterIsInstance<OutboundTelegramMessageAction.Callback>()
    }

    private fun player(nickname: String): SaoOnlinePlayer =
        SaoOnlinePlayer(
            playerId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
            nickname = nickname,
            accountId = null,
            observedAt = Instant.parse("2026-08-30T12:24:00Z"),
        )
}
