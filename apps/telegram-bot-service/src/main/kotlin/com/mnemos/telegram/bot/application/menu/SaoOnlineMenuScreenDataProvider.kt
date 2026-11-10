package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.online.SaoOnlinePlayerMatcher
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayerSearchMatch
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import com.mnemos.telegram.bot.application.online.port.out.SaoOnlineSnapshotGateway
import org.springframework.stereotype.Component

@Component
class SaoOnlineMenuScreenDataProvider(
    private val snapshotGateway: SaoOnlineSnapshotGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ONLINE

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData {
        if (session.pendingInput == TelegramPendingInput.ONLINE_PLAYER_SEARCH) {
            return TelegramMenuScreenData.SaoOnline(
                SaoOnlineMenuScreenData.SearchPrompt(
                    error = session.screenContext[SaoOnlineMenuContext.SEARCH_ERROR_KEY],
                ),
            )
        }

        return TelegramMenuScreenData.SaoOnline(
            data = resolveScreenData(snapshotGateway.getSnapshot(), session.screenContext),
        )
    }

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
    ): SaoOnlineMenuScreenData =
        when (context[SaoOnlineMenuContext.MODE_KEY]) {
            SaoOnlineMenuContext.MODE_REALM -> resolveRealmDetail(snapshot, context)
            SaoOnlineMenuContext.MODE_SEARCH -> resolveSearchResults(snapshot, context)
            else -> SaoOnlineMenuScreenData.RealmList(snapshot)
        }

    private fun resolveRealmDetail(
        snapshot: SaoOnlineSnapshot,
        context: Map<String, String>,
    ): SaoOnlineMenuScreenData {
        val realmNumber = context[SaoOnlineMenuContext.REALM_KEY]?.toIntOrNull()
        val realm =
            snapshot.realms.firstOrNull { candidate -> candidate.number == realmNumber }
                ?: return SaoOnlineMenuScreenData.RealmList(snapshot)
        val players =
            realm.players.sortedWith(
                compareBy<SaoOnlinePlayer, String>(String.CASE_INSENSITIVE_ORDER) { player -> player.nickname },
            )
        val page = resolvePage(context, players.size)
        val totalPages = pageCount(players.size)
        val pagePlayers = players.page(page)

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

    private fun resolveSearchResults(
        snapshot: SaoOnlineSnapshot,
        context: Map<String, String>,
    ): SaoOnlineMenuScreenData {
        val query =
            context[SaoOnlineMenuContext.SEARCH_QUERY_KEY]
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: return SaoOnlineMenuScreenData.RealmList(snapshot)
        val matches = SaoOnlinePlayerMatcher.findMatches(snapshot, query)
        val page = resolvePage(context, matches.size)

        return SaoOnlineMenuScreenData.SearchResults(
            generatedAt = snapshot.generatedAt,
            query = query,
            totalMatches = matches.size,
            page = page,
            totalPages = pageCount(matches.size),
            matches = matches.page(page),
        )
    }

    private fun resolvePage(
        context: Map<String, String>,
        itemCount: Int,
    ): Int {
        val totalPages = pageCount(itemCount)
        val requestedPage = context[SaoOnlineMenuContext.PAGE_KEY]?.toIntOrNull() ?: 0

        return requestedPage.coerceIn(0, totalPages - 1)
    }

    private fun pageCount(itemCount: Int): Int = maxOf(1, (itemCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE)

    private fun <T> List<T>.page(page: Int): List<T> = drop(page * ITEMS_PER_PAGE).take(ITEMS_PER_PAGE)

    private companion object {
        const val ITEMS_PER_PAGE = 10
    }
}
