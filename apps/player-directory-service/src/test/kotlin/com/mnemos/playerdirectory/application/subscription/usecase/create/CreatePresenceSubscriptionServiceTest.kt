package com.mnemos.playerdirectory.application.subscription.usecase.create

import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerGateway
import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameHistoryRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerPresenceTelemetry
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.application.subscription.exception.PlayerPresenceNotObservableException
import com.mnemos.playerdirectory.application.subscription.exception.PlayerPresenceVerificationUnavailableException
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionIntegrationEventPublisher
import com.mnemos.playerdirectory.application.subscription.port.out.PresenceSubscriptionRepository
import com.mnemos.playerdirectory.application.subscription.support.PresenceSubscriptionCoordinator
import com.mnemos.playerdirectory.application.tracking.policy.PlayerTrackingSchedulePolicy
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameHistoryEntry
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameOwnership
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresence
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import com.mnemos.playerdirectory.domain.player.policy.PlayerPresenceTransitionPolicy
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class CreatePresenceSubscriptionServiceTest {
    private val playerId = CristalixPlayerId(UUID.fromString("ea0a316c-ec21-11ea-acca-1cb72caa35fd"))
    private val subscriberUserId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")
    private val observedAt = Instant.parse("2026-08-27T00:00:00Z")

    @Test
    fun `online player can be subscribed to after immediate verification`() {
        val fixture = fixture(PlayerPresenceStatus.ONLINE)

        val subscription = fixture.service.execute(command())

        assertEquals(playerId, subscription.playerId)
        assertNotNull(fixture.playerRepository.findById(playerId))
        assertNotNull(fixture.subscriptionRepository.findBySubscriberUserIdAndPlayerId(subscriberUserId, playerId))
        assertNotNull(fixture.trackingRepository.findByIdForUpdate(playerId))
    }

    @Test
    fun `offline player can be subscribed to after immediate verification`() {
        val fixture = fixture(PlayerPresenceStatus.OFFLINE)

        fixture.service.execute(command())

        assertNotNull(fixture.subscriptionRepository.findBySubscriberUserIdAndPlayerId(subscriberUserId, playerId))
        assertNotNull(fixture.trackingRepository.findByIdForUpdate(playerId))
    }

    @Test
    fun `unknown player presence rejects subscription without persisting it`() {
        val fixture = fixture(PlayerPresenceStatus.UNKNOWN)

        assertThrows(PlayerPresenceVerificationUnavailableException::class.java) {
            fixture.service.execute(command())
        }

        assertNull(fixture.playerRepository.findById(playerId))
        assertNull(fixture.subscriptionRepository.findBySubscriberUserIdAndPlayerId(subscriberUserId, playerId))
        assertNull(fixture.trackingRepository.findByIdForUpdate(playerId))
    }

    @Test
    fun `hidden player presence rejects subscription without persisting it`() {
        val fixture = fixture(PlayerPresenceStatus.HIDDEN)

        assertThrows(PlayerPresenceNotObservableException::class.java) {
            fixture.service.execute(command())
        }

        assertNull(fixture.playerRepository.findById(playerId))
        assertNull(fixture.subscriptionRepository.findBySubscriberUserIdAndPlayerId(subscriberUserId, playerId))
        assertNull(fixture.trackingRepository.findByIdForUpdate(playerId))
    }

    private fun command(): CreatePresenceSubscriptionCommand =
        CreatePresenceSubscriptionCommand(
            subscriberUserId = subscriberUserId,
            playerId = playerId,
        )

    private fun fixture(status: PlayerPresenceStatus): Fixture {
        val playerRepository = InMemoryPlayerRepository()
        val nicknameOwnershipRepository = InMemoryNicknameOwnershipRepository()
        val nicknameHistoryRepository = InMemoryNicknameHistoryRepository()
        val subscriptionRepository = InMemoryPresenceSubscriptionRepository()
        val trackingRepository = InMemoryPlayerTrackingRepository()
        val gateway = FakeCristalixPlayerGateway(observation(status))
        val observationCoordinator =
            PlayerObservationCoordinator(
                playerRepository = playerRepository,
                nicknameOwnershipRepository = nicknameOwnershipRepository,
                nicknameHistoryRepository = nicknameHistoryRepository,
                transitionPolicy =
                    PlayerPresenceTransitionPolicy(
                        offlineConfirmationDelay = Duration.ofSeconds(20),
                        requiredOfflineObservations = 2,
                    ),
                eventPublisher = NoOpPlayerIntegrationEventPublisher,
                presenceTelemetry = PlayerPresenceTelemetry { },
            )
        val trackingSchedulePolicy =
            PlayerTrackingSchedulePolicy(
                subscriptionPollInterval = Duration.ofSeconds(20),
                saoOnlinePollInterval = Duration.ofSeconds(30),
                highActivityPollInterval = Duration.ofMinutes(1),
                lowActivityPollInterval = Duration.ofMinutes(5),
                unverifiablePollInterval = Duration.ofMinutes(2),
                failureBackoff = Duration.ofSeconds(30),
                maximumFailureBackoff = Duration.ofMinutes(10),
            )
        val subscriptionCoordinator =
            PresenceSubscriptionCoordinator(
                repository = subscriptionRepository,
                trackingRepository = trackingRepository,
                trackingSchedulePolicy = trackingSchedulePolicy,
                eventPublisher = NoOpPresenceSubscriptionIntegrationEventPublisher,
            )

        return Fixture(
            service =
                CreatePresenceSubscriptionService(
                    cristalixPlayerGateway = gateway,
                    playerObservationCoordinator = observationCoordinator,
                    presenceSubscriptionRepository = subscriptionRepository,
                    presenceSubscriptionCoordinator = subscriptionCoordinator,
                ),
            playerRepository = playerRepository,
            subscriptionRepository = subscriptionRepository,
            trackingRepository = trackingRepository,
        )
    }

    private fun observation(status: PlayerPresenceStatus): PlayerObservation =
        PlayerObservation(
            playerId = playerId,
            nickname = PlayerNickname.of("KoTuK_PvP"),
            externalVersion = 485,
            externalUpdatedAt = Instant.parse("2026-04-15T01:20:03.944Z"),
            presence =
                PlayerPresence(
                    status = status,
                    realm = if (status == PlayerPresenceStatus.ONLINE) "SAO-3" else null,
                    realmType = if (status == PlayerPresenceStatus.ONLINE) "SAO" else null,
                    privacyHidden = status == PlayerPresenceStatus.HIDDEN,
                    lastSeenOnline = Instant.parse("2026-08-26T23:59:30Z"),
                    observedAt = observedAt,
                ),
        )

    private data class Fixture(
        val service: CreatePresenceSubscriptionService,
        val playerRepository: InMemoryPlayerRepository,
        val subscriptionRepository: InMemoryPresenceSubscriptionRepository,
        val trackingRepository: InMemoryPlayerTrackingRepository,
    )

    private class FakeCristalixPlayerGateway(
        private val observation: PlayerObservation?,
    ) : CristalixPlayerGateway {
        override fun findById(playerId: CristalixPlayerId): PlayerObservation? = observation

        override fun findByNickname(nickname: PlayerNickname): PlayerObservation? = observation
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
            ownerships[ownership.nickname.normalized()] = ownership

            return ownership
        }

        override fun delete(ownership: PlayerNicknameOwnership) {
            ownerships.remove(ownership.nickname.normalized())
        }

        override fun findByNickname(nickname: PlayerNickname): PlayerNicknameOwnership? = ownerships[nickname.normalized()]

        override fun findByNormalizedNicknamesForUpdate(normalizedNicknames: Set<String>): List<PlayerNicknameOwnership> =
            normalizedNicknames.mapNotNull(ownerships::get)
    }

    private class InMemoryNicknameHistoryRepository : PlayerNicknameHistoryRepository {
        private val entries = mutableMapOf<UUID, PlayerNicknameHistoryEntry>()

        override fun save(entry: PlayerNicknameHistoryEntry): PlayerNicknameHistoryEntry {
            entries[entry.id.id] = entry

            return entry
        }

        override fun findOpenByPlayerIdForUpdate(playerId: CristalixPlayerId): PlayerNicknameHistoryEntry? =
            entries.values.firstOrNull { entry -> entry.playerId == playerId && entry.validUntil == null }
    }

    private class InMemoryPresenceSubscriptionRepository : PresenceSubscriptionRepository {
        private val subscriptions = mutableListOf<PresenceSubscription>()

        override fun save(subscription: PresenceSubscription): PresenceSubscription {
            subscriptions.removeIf { current -> current.id == subscription.id }
            subscriptions += subscription

            return subscription
        }

        override fun delete(subscription: PresenceSubscription) {
            subscriptions.removeIf { current -> current.id == subscription.id }
        }

        override fun findBySubscriberUserIdAndPlayerId(
            subscriberUserId: UUID,
            playerId: CristalixPlayerId,
        ): PresenceSubscription? =
            subscriptions.firstOrNull { subscription ->
                subscription.subscriberUserId == subscriberUserId && subscription.playerId == playerId
            }

        override fun findAllBySubscriberUserId(subscriberUserId: UUID): List<PresenceSubscription> =
            subscriptions.filter { subscription -> subscription.subscriberUserId == subscriberUserId }

        override fun existsByPlayerId(playerId: CristalixPlayerId): Boolean =
            subscriptions.any { subscription -> subscription.playerId == playerId }
    }

    private class InMemoryPlayerTrackingRepository : PlayerTrackingRepository {
        private val states = mutableMapOf<CristalixPlayerId, PlayerTrackingState>()

        override fun ensureState(state: PlayerTrackingState) {
            val current = states[state.playerId]

            states[state.playerId] =
                if (current == null || state.nextPollAt.isBefore(current.nextPollAt)) {
                    state
                } else {
                    current
                }
        }

        override fun save(state: PlayerTrackingState): PlayerTrackingState {
            states[state.playerId] = state

            return state
        }

        override fun findByIdForUpdate(playerId: CristalixPlayerId): PlayerTrackingState? = states[playerId]

        override fun findDueForUpdate(
            now: Instant,
            limit: Int,
        ): List<PlayerTrackingState> =
            states.values
                .filter { state -> !state.nextPollAt.isAfter(now) }
                .take(limit)
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

    private object NoOpPresenceSubscriptionIntegrationEventPublisher : PresenceSubscriptionIntegrationEventPublisher {
        override fun publishCreated(
            subscription: PresenceSubscription,
            occurredAt: Instant,
        ) = Unit

        override fun publishDeleted(
            subscription: PresenceSubscription,
            occurredAt: Instant,
        ) = Unit
    }
}
