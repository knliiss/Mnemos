package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.online.SaoOnlineCallbackData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayerSearchMatch
import com.mnemos.telegram.bot.application.online.model.SaoOnlineRealm
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LocalTelegramMenuMessageRendererTest {
    private val renderer = LocalTelegramMenuMessageRenderer()

    @Test
    fun `realm list renders live snapshot and search actions`() {
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

        val message = renderer.render(TelegramMenuScreen.ONLINE, data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Сейчас отслеживается: <b>58</b>"))
        assertTrue(message.text.contains("SAO-1 — <b>21</b>"))
        assertTrue(message.text.contains("SAO-2 — <b>37</b>"))
        assertTrue(message.text.contains("30.08 12:24 UTC"))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.SEARCH })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == "on:r:1" })
        assertTrue(callbacks.any { action -> action.data == "on:r:2" })
    }

    @Test
    fun `realm detail renders paginated players and escapes nickname html`() {
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

        val message = renderer.render(TelegramMenuScreen.ONLINE, data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("<b>SAO-4</b>"))
        assertTrue(message.text.contains("Страница: <b>2/2</b>"))
        assertTrue(message.text.contains("11. <code>player11</code>"))
        assertTrue(message.text.contains("12. <code>name&lt;12&gt;</code>"))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.PREVIOUS_PAGE })
        assertTrue(callbacks.none { action -> action.data == SaoOnlineCallbackData.NEXT_PAGE })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REALM_LIST })
    }

    @Test
    fun `search prompt renders validation error and cancel action`() {
        val data =
            TelegramMenuScreenData.SaoOnline(
                SaoOnlineMenuScreenData.SearchPrompt(
                    error = "invalid <query>",
                ),
            )

        val message = renderer.render(TelegramMenuScreen.ONLINE, data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Отправьте никнейм или его часть"))
        assertTrue(message.text.contains("invalid &lt;query&gt;"))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.SEARCH_CANCEL })
    }

    @Test
    fun `search results render realm matches and escape user text`() {
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

        val message = renderer.render(TelegramMenuScreen.ONLINE, data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Запрос: <code>name&lt;</code>"))
        assertTrue(message.text.contains("<code>name&lt;one&gt;</code> — SAO-1"))
        assertTrue(message.text.contains("Найдено: <b>2</b>"))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.SEARCH })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REFRESH })
    }

    @Test
    fun `online screen renders recoverable unavailable state`() {
        val data = TelegramMenuScreenData.SaoOnline(SaoOnlineMenuScreenData.Unavailable)

        val message = renderer.render(TelegramMenuScreen.ONLINE, data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Не удалось получить актуальный онлайн"))
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == SaoOnlineCallbackData.SEARCH })
    }

    private fun player(nickname: String): SaoOnlinePlayer =
        SaoOnlinePlayer(
            playerId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
            nickname = nickname,
            accountId = null,
            observedAt = Instant.parse("2026-08-30T12:24:00Z"),
        )
}
