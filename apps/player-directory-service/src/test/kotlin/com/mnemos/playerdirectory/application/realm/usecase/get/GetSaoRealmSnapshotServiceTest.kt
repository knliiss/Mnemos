package com.mnemos.playerdirectory.application.realm.usecase.get

import com.mnemos.playerdirectory.application.realm.model.SaoRealmPlayer
import com.mnemos.playerdirectory.application.realm.policy.SaoRealmReadPolicy
import com.mnemos.playerdirectory.application.realm.port.out.SaoRealmReader
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class GetSaoRealmSnapshotServiceTest {
    @Test
    fun `groups only canonical SAO realms and sorts numeric suffixes`() {
        val now = Instant.parse("2026-08-27T00:20:00Z")
        val reader =
            StubSaoRealmReader(
                players =
                    listOf(
                        player("00000000-0000-0000-0000-000000000010", "Zulu", "SAO-10", now),
                        player("00000000-0000-0000-0000-000000000002", "Beta", "SAO-2", now),
                        player("00000000-0000-0000-0000-000000000001", "alpha", "SAO-2", now),
                        player("00000000-0000-0000-0000-000000000003", "Legacy", "SAO-01", now),
                        player("00000000-0000-0000-0000-000000000004", "Lobby", "LOBBY", now),
                    ),
            )
        val service =
            GetSaoRealmSnapshotService(
                reader = reader,
                policy =
                    SaoRealmReadPolicy(
                        freshness = Duration.ofMinutes(1),
                        cacheTtl = Duration.ofSeconds(10),
                    ),
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        val snapshot = service.execute()

        assertEquals(now.minusSeconds(60), reader.lastCutoff)
        assertEquals(3, snapshot.trackedOnline)
        assertEquals(listOf("SAO-2", "SAO-10"), snapshot.realms.map { realm -> realm.name.value })
        assertEquals(
            listOf("alpha", "Beta"),
            snapshot.realms
                .first()
                .players
                .map(SaoRealmPlayer::nickname),
        )
    }

    private fun player(
        id: String,
        nickname: String,
        realm: String,
        observedAt: Instant,
    ): SaoRealmPlayer =
        SaoRealmPlayer(
            playerId = CristalixPlayerId(UUID.fromString(id)),
            nickname = nickname,
            accountId = null,
            realm = realm,
            observedAt = observedAt,
        )

    private class StubSaoRealmReader(
        private val players: List<SaoRealmPlayer>,
    ) : SaoRealmReader {
        var lastCutoff: Instant? = null
            private set

        override fun findFreshOnlinePlayers(cutoff: Instant): List<SaoRealmPlayer> {
            lastCutoff = cutoff
            return players
        }
    }
}
