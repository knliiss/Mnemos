package com.mnemos.playerdirectory.application.player.support

import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameHistoryRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerPresenceTelemetry
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameHistoryEntry
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameOwnership
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresence
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import com.mnemos.playerdirectory.domain.player.policy.PlayerPresenceTransitionPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class PlayerObservationCoordinatorTest {
    private val firstPlayerId = CristalixPlayerId(UUID.fromString("0198f6ce-e934-7000-8000-000000000001"))
    private val secondPlayerId = CristalixPlayerId(UUID.fromString("0198f6ce-e934-7000-8000-000000000002"))

    @Test
    fun `nickname ownership moves to the currently observed player`() {
        val playerRepository = InMemoryPlayerRepository()
        val ownershipRepository = InMemoryNicknameOwnershipRepository()
        val historyRepository = InMemoryNicknameHistoryRepository()
        val coordinator = coordinator(playerRepository, ownershipRepository, historyRepository)

        coordinator.apply(observation(firstPlayerId, "SharedName", 1, "2026-08-27T00:00:00Z"))
        coordinator.apply(observation(secondPlayerId, "SecondName", 1, "2026-08-27T00:01:00Z"))
        coordinator.apply(observation(secondPlayerId, "SharedName", 2, "2026-08-27T00:02:00Z"))

        val currentOwner = ownershipRepository.findByNickname(PlayerNickname.of("SharedName"))
        val formerNameOwner = ownershipRepository.findByNickname(PlayerNickname.of("SecondName"))
        val firstPlayerHistory = historyRepository.entriesFor(firstPlayerId)
        val secondPlayerHistory = historyRepository.entriesFor(secondPlayerId)

        assertEquals(secondPlayerId, currentOwner?.playerId)
        assertNull(formerNameOwner)
        assertEquals(Instant.parse("2026-08-27T00:02:00Z"), firstPlayerHistory.single().validUntil)
        assertEquals("SharedName", secondPlayerHistory.single { entry -> entry.validUntil == null }.nickname.value)
    }

    @Test
    fun `stale observation cannot steal current nickname ownership`() {
        val playerRepository = InMemoryPlayerRepository()
        val ownershipRepository = InMemoryNicknameOwnershipRepository()
        val historyRepository = InMemoryNicknameHistoryRepository()
        val coordinator = coordinator(playerRepository, ownershipRepository, historyRepository)

        coordinator.apply(observation(firstPlayerId, "CurrentName", 10, "2026-08-27T00:10:00Z"))
        coordinator.apply(observation(firstPlayerId, "StaleName", 9, "2026-08-27T00:11:00Z"))

        assertEquals(firstPlayerId, ownershipRepository.findByNickname(PlayerNickname.of("CurrentName"))?.playerId)
        assertNull(ownershipRepository.findByNickname(PlayerNickname.of("StaleName")))
    }

    @Test
    fun `confirmed offline transition records detection latency`() {
        val playerRepository = InMemoryPlayerRepository()
        val ownershipRepository = InMemoryNicknameOwnershipRepository()
        val historyRepository = InMemoryNicknameHistoryRepository()
        val telemetry = RecordingPlayerPresenceTelemetry()
        val coordinator =
            coordinator(
                playerRepository = playerRepository,
                ownershipRepository = ownershipRepository,
                historyRepository = historyRepository,
                presenceTelemetry = telemetry,
            )

        coordinator.apply(
            observation(
                playerId = firstPlayerId,
                nickname = "TrackedPlayer",
                version = 1,
                observedAt = "2026-08-27T00:00:00Z",
            ),
        )
        coordinator.apply(
            observation(
                playerId = firstPlayerId,
                nickname = "TrackedPlayer",
                version = 2,
                observedAt = "2026-08-27T00:00:01Z",
                status = PlayerPresenceStatus.OFFLINE,
            ),
        )
        coordinator.apply(
            observation(
                playerId = firstPlayerId,
                nickname = "TrackedPlayer",
                version = 3,
                observedAt = "2026-08-27T00:00:11Z",
                status = PlayerPresenceStatus.OFFLINE,
            ),
        )

        assertEquals(listOf(Duration.ofSeconds(10)), telemetry.offlineDetectionLatencies)
    }

    private fun coordinator(
        playerRepository: PlayerRepository,
        ownershipRepository: PlayerNicknameOwnershipRepository,
        historyRepository: PlayerNicknameHistoryRepository,
        presenceTelemetry: PlayerPresenceTelemetry = NoOpPlayerPresenceTelemetry,
    ): PlayerObservationCoordinator =
        PlayerObservationCoordinator(
            playerRepository = playerRepository,
            nicknameOwnershipRepository = ownershipRepository,
            nicknameHistoryRepository = historyRepository,
            transitionPolicy = PlayerPresenceTransitionPolicy(Duration.ofSeconds(10), 2),
            eventPublisher = NoOpPlayerIntegrationEventPublisher,
            presenceTelemetry = presenceTelemetry,
        )

    private fun observation(
        playerId: CristalixPlayerId,
        nickname: String,
        version: Long,
        observedAt: String,
        status: PlayerPresenceStatus = PlayerPresenceStatus.ONLINE,
    ): PlayerObservation {
        val online = status == PlayerPresenceStatus.ONLINE

        return PlayerObservation(
            playerId = playerId,
            nickname = PlayerNickname.of(nickname),
            externalVersion = version,
            externalUpdatedAt = Instant.parse(observedAt),
            presence =
                PlayerPresence(
                    status = status,
                    realm = if (online) "SAO-1" else null,
                    realmType = if (online) "SAO" else null,
                    privacyHidden = false,
                    lastSeenOnline = null,
                    observedAt = Instant.parse(observedAt),
                ),
        )
    }

    private class InMemoryPlayerRepository : PlayerRepository {
        private val players = mutableMapOf<CristalixPlayerId, Player>()

        override fun ensureState(player: Player): Boolean {
            if (players.containsKey(player.id)) {
                return false
            }

            players[player.id] = player
            return true
        }

        override fun save(player: Player): Player {
            players[player.id] = player
            return player
        }

        override fun findById(playerId: CristalixPlayerId): Player? = players[playerId]

        override fun findByIdForUpdate(playerId: CristalixPlayerId): Player? = players[playerId]
    }

    private class InMemoryNicknameOwnershipRepository : PlayerNicknameOwnershipRepository {
        private val ownerships = mutableMapOf<String, PlayerNicknameOwnership>()

        override fun save(ownership: PlayerNicknameOwnership): PlayerNicknameOwnership {
            val previousPlayerOwnership =
                ownerships.values.firstOrNull { current ->
                    current.playerId == ownership.playerId &&
                        current.nickname.normalized() != ownership.nickname.normalized()
                }

            if (previousPlayerOwnership != null) {
                ownerships.remove(previousPlayerOwnership.nickname.normalized())
            }

            ownerships[ownership.nickname.normalized()] = ownership
            return ownership
        }

        override fun delete(ownership: PlayerNicknameOwnership) {
            ownerships.remove(ownership.nickname.normalized())
        }

        override fun findByNickname(nickname: PlayerNickname): PlayerNicknameOwnership? = ownerships[nickname.normalized()]

        override fun findByNormalizedNicknamesForUpdate(normalizedNicknames: Set<String>): List<PlayerNicknameOwnership> =
            normalizedNicknames.sorted().mapNotNull(ownerships::get)
    }

    private class InMemoryNicknameHistoryRepository : PlayerNicknameHistoryRepository {
        private val entries = mutableMapOf<UUID, PlayerNicknameHistoryEntry>()

        override fun save(entry: PlayerNicknameHistoryEntry): PlayerNicknameHistoryEntry {
            entries[entry.id.id] = entry
            return entry
        }

        override fun findOpenByPlayerIdForUpdate(playerId: CristalixPlayerId): PlayerNicknameHistoryEntry? =
            entries.values.firstOrNull { entry -> entry.playerId == playerId && entry.validUntil == null }

        fun entriesFor(playerId: CristalixPlayerId): List<PlayerNicknameHistoryEntry> =
            entries.values
                .filter { entry -> entry.playerId == playerId }
                .sortedBy(PlayerNicknameHistoryEntry::validFrom)
    }

    private class RecordingPlayerPresenceTelemetry : PlayerPresenceTelemetry {
        val offlineDetectionLatencies = mutableListOf<Duration>()

        override fun recordOfflineDetectionLatency(latency: Duration) {
            offlineDetectionLatencies += latency
        }
    }

    private object NoOpPlayerPresenceTelemetry : PlayerPresenceTelemetry {
        override fun recordOfflineDetectionLatency(latency: Duration) = Unit
    }

    private object NoOpPlayerIntegrationEventPublisher : PlayerIntegrationEventPublisher {
        override fun publishInitial(
            player: Player,
            observedAt: Instant,
        ) = Unit

        override fun publishChanges(
            previous: Player,
            current: Player,
            observedAt: Instant,
        ) = Unit
    }
}
