package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer
import com.mnemos.telegram.bot.application.online.model.SaoOnlineRealm
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import com.mnemos.telegram.bot.application.online.port.out.SaoOnlineSnapshotGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SaoOnlineMenuScreenDataProviderTest {
    @Test
    fun `realm context selects page and sorts players by nickname`() {
        val snapshot = snapshotWithPlayers(12)
        val provider = SaoOnlineMenuScreenDataProvider(FixedGateway(SaoOnlineSnapshotResult.Available(snapshot)))
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.ONLINE,
                screenContext = SaoOnlineMenuContext.realm(realmNumber = 4, page = 1),
            )

        val data = provider.load(session) as TelegramMenuScreenData.SaoOnline
        val detail = data.data as SaoOnlineMenuScreenData.RealmDetail

        assertEquals("SAO-4", detail.realmName)
        assertEquals(1, detail.page)
        assertEquals(2, detail.totalPages)
        assertEquals(listOf("player11", "player12"), detail.players.map { player -> player.nickname })
    }

    @Test
    fun `page is clamped when realm population shrinks`() {
        val snapshot = snapshotWithPlayers(3)
        val provider = SaoOnlineMenuScreenDataProvider(FixedGateway(SaoOnlineSnapshotResult.Available(snapshot)))
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.ONLINE,
                screenContext = SaoOnlineMenuContext.realm(realmNumber = 4, page = 8),
            )

        val data = provider.load(session) as TelegramMenuScreenData.SaoOnline
        val detail = data.data as SaoOnlineMenuScreenData.RealmDetail

        assertEquals(0, detail.page)
        assertEquals(1, detail.totalPages)
        assertEquals(3, detail.players.size)
    }

    @Test
    fun `missing realm falls back to realm list`() {
        val snapshot = snapshotWithPlayers(3)
        val provider = SaoOnlineMenuScreenDataProvider(FixedGateway(SaoOnlineSnapshotResult.Available(snapshot)))
        val session =
            TelegramMenuSession(
                telegramId = 931458258L,
                currentScreen = TelegramMenuScreen.ONLINE,
                screenContext = SaoOnlineMenuContext.realm(realmNumber = 99),
            )

        val data = provider.load(session) as TelegramMenuScreenData.SaoOnline
        val list = data.data as SaoOnlineMenuScreenData.RealmList

        assertSame(snapshot, list.snapshot)
    }

    private fun snapshotWithPlayers(playerCount: Int): SaoOnlineSnapshot {
        val players =
            (1..playerCount)
                .map { number -> player("player%02d".format(number)) }
                .reversed()

        return SaoOnlineSnapshot(
            generatedAt = Instant.parse("2026-08-30T12:24:00Z"),
            trackedOnline = playerCount,
            realms =
                listOf(
                    SaoOnlineRealm(
                        name = "SAO-4",
                        number = 4,
                        playerCount = playerCount,
                        players = players,
                    ),
                ),
        )
    }

    private fun player(nickname: String): SaoOnlinePlayer =
        SaoOnlinePlayer(
            playerId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
            nickname = nickname,
            accountId = null,
            observedAt = Instant.parse("2026-08-30T12:24:00Z"),
        )

    private class FixedGateway(
        private val result: SaoOnlineSnapshotResult,
    ) : SaoOnlineSnapshotGateway {
        override fun getSnapshot(): SaoOnlineSnapshotResult = result
    }
}
