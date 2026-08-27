package com.mnemos.playerdirectory.application.realm.usecase.get

import com.mnemos.playerdirectory.application.realm.model.SaoRealm
import com.mnemos.playerdirectory.application.realm.model.SaoRealmName
import com.mnemos.playerdirectory.application.realm.model.SaoRealmPlayer
import com.mnemos.playerdirectory.application.realm.model.SaoRealmSnapshot
import com.mnemos.playerdirectory.application.realm.policy.SaoRealmReadPolicy
import com.mnemos.playerdirectory.application.realm.port.out.SaoRealmReader
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class GetSaoRealmSnapshotService(
    private val reader: SaoRealmReader,
    private val policy: SaoRealmReadPolicy,
    private val clock: Clock,
) : GetSaoRealmSnapshotUseCase {
    private val refreshLock = ReentrantLock()

    @Volatile
    private var cacheEntry: CacheEntry? = null

    override fun execute(): SaoRealmSnapshot {
        val now = clock.instant()
        cachedSnapshot(now)?.let { snapshot -> return snapshot }

        return refreshLock.withLock {
            val refreshNow = clock.instant()
            cachedSnapshot(refreshNow)
                ?: rebuild(refreshNow).also { snapshot ->
                    cacheEntry =
                        CacheEntry(
                            snapshot = snapshot,
                            expiresAt = refreshNow.plus(policy.cacheTtl),
                        )
                }
        }
    }

    private fun cachedSnapshot(now: Instant): SaoRealmSnapshot? =
        cacheEntry
            ?.takeIf { entry -> entry.expiresAt.isAfter(now) }
            ?.snapshot

    private fun rebuild(generatedAt: Instant): SaoRealmSnapshot {
        val players = reader.findFreshOnlinePlayers(generatedAt.minus(policy.freshness))
        val grouped =
            players
                .mapNotNull { player ->
                    SaoRealmName.parseOrNull(player.realm)?.let { realm -> realm to player }
                }.groupBy(
                    keySelector = { (realm, _) -> realm },
                    valueTransform = { (_, player) -> player },
                )
        val realms =
            grouped
                .entries
                .sortedBy { (realm, _) -> realm }
                .map { (realm, realmPlayers) ->
                    SaoRealm(
                        name = realm,
                        players = realmPlayers.sortedWith(playerComparator),
                    )
                }
        val trackedOnline = realms.sumOf { realm -> realm.players.size }

        return SaoRealmSnapshot(
            generatedAt = generatedAt,
            trackedOnline = trackedOnline,
            realms = realms,
        )
    }

    private data class CacheEntry(
        val snapshot: SaoRealmSnapshot,
        val expiresAt: Instant,
    )

    private companion object {
        val playerComparator =
            compareBy<SaoRealmPlayer> { player -> player.nickname.lowercase(Locale.ROOT) }
                .thenBy { player -> player.playerId.id }
    }
}
