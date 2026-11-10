package com.mnemos.telegram.bot.application.online

import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayerSearchMatch
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot

object SaoOnlinePlayerMatcher {
    fun findMatches(snapshot: SaoOnlineSnapshot, query: String): List<SaoOnlinePlayerSearchMatch> {
        val matches =
            snapshot.realms.flatMap { realm ->
                realm.players
                    .filter { player -> player.nickname.contains(query, ignoreCase = true) }
                    .map { player ->
                        SaoOnlinePlayerSearchMatch(
                            nickname = player.nickname,
                            realmName = realm.name,
                            realmNumber = realm.number,
                        )
                    }
            }
        val exactMatches = matches.filter { match -> match.nickname.equals(query, ignoreCase = true) }
        val partialMatches = matches.filterNot { match -> match.nickname.equals(query, ignoreCase = true) }
        return sortMatches(exactMatches) + sortMatches(partialMatches)
    }

    private fun sortMatches(matches: List<SaoOnlinePlayerSearchMatch>): List<SaoOnlinePlayerSearchMatch> =
        matches.sortedWith(
            compareBy<SaoOnlinePlayerSearchMatch, String>(String.CASE_INSENSITIVE_ORDER) { match -> match.nickname }
                .thenBy { match -> match.realmNumber },
        )
}
