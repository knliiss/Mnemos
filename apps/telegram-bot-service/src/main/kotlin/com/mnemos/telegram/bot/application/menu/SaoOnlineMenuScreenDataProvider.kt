package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import com.mnemos.telegram.bot.application.online.port.out.SaoOnlineSnapshotGateway
import org.springframework.stereotype.Component

@Component
class SaoOnlineMenuScreenDataProvider(
    private val snapshotGateway: SaoOnlineSnapshotGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ONLINE

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.SaoOnline(
            data = resolveScreenData(snapshotGateway.getSnapshot(), session.screenContext),
        )

    private fun resolveScreenData(
        result: SaoOnlineSnapshotResult,
        context: Map<String, String>,
    ): SaoOnlineMenuScreenData =
        when (result) {
            is SaoOnlineSnapshotResult.Available -> resolveAvailableScreenData(result.snapshot, context)
            SaoOnlineSnapshotResult.Unavailable -> SaoOnlineMenuScreenData.Unavailable
        }

    private fun resolveAvailableScreenData(
        snapshot: SaoOnlineSnapshot,
        context: Map<String, String>,
    ): SaoOnlineMenuScreenData {
        if (context[SaoOnlineMenuContext.MODE_KEY] != SaoOnlineMenuContext.MODE_REALM) {
            return SaoOnlineMenuScreenData.RealmList(snapshot)
        }

        val realmNumber = context[SaoOnlineMenuContext.REALM_KEY]?.toIntOrNull()
        val realm =
            snapshot.realms.firstOrNull { candidate -> candidate.number == realmNumber }
                ?: return SaoOnlineMenuScreenData.RealmList(snapshot)
        val players =
            realm.players.sortedWith(
                compareBy<SaoOnlinePlayer>(String.CASE_INSENSITIVE_ORDER) { player -> player.nickname },
            )
        val totalPages = maxOf(1, (players.size + PLAYERS_PER_PAGE - 1) / PLAYERS_PER_PAGE)
        val requestedPage = context[SaoOnlineMenuContext.PAGE_KEY]?.toIntOrNull() ?: 0
        val page = requestedPage.coerceIn(0, totalPages - 1)
        val pageStart = page * PLAYERS_PER_PAGE
        val pagePlayers = players.drop(pageStart).take(PLAYERS_PER_PAGE)

        return SaoOnlineMenuScreenData.RealmDetail(
            generatedAt = snapshot.generatedAt,
            realmName = realm.name,
            realmNumber = realm.number,
            playerCount = realm.playerCount,
            page = page,
            totalPages = totalPages,
            players = pagePlayers,
        )
    }

    private companion object {
        const val PLAYERS_PER_PAGE = 10
    }
}
