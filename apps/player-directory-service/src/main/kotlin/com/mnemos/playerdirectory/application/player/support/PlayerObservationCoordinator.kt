package com.mnemos.playerdirectory.application.player.support

import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameHistoryRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameHistoryEntry
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameOwnership
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.policy.PlayerPresenceTransitionPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PlayerObservationCoordinator(
    private val playerRepository: PlayerRepository,
    private val nicknameOwnershipRepository: PlayerNicknameOwnershipRepository,
    private val nicknameHistoryRepository: PlayerNicknameHistoryRepository,
    private val transitionPolicy: PlayerPresenceTransitionPolicy,
    private val eventPublisher: PlayerIntegrationEventPublisher,
) {
    @Transactional
    fun apply(observation: PlayerObservation): Player {
        playerRepository.ensureState(Player.fromObservation(observation))

        val current =
            requireNotNull(playerRepository.findByIdForUpdate(observation.playerId)) {
                "Player state must exist after ensureState"
            }

        if (observation.externalVersion < current.externalVersion) {
            return current
        }

        synchronizeNicknameOwnership(
            current = current,
            observation = observation,
        )

        val player = current.applyObservation(observation, transitionPolicy)

        if (player == current) {
            return player
        }

        val saved = playerRepository.save(player)

        eventPublisher.publishChanges(
            previous = current,
            current = saved,
            observedAt = observation.presence.observedAt,
        )

        return saved
    }

    private fun synchronizeNicknameOwnership(
        current: Player,
        observation: PlayerObservation,
    ) {
        val currentNickname = current.nickname
        val observedNickname = observation.nickname
        val normalizedNicknames =
            setOf(
                currentNickname.normalized(),
                observedNickname.normalized(),
            )
        val lockedOwnerships =
            nicknameOwnershipRepository.findByNormalizedNicknamesForUpdate(normalizedNicknames)
        val currentOwnership =
            lockedOwnerships.firstOrNull { ownership ->
                ownership.playerId == current.id
            }
        val observedNicknameOwner =
            lockedOwnerships.firstOrNull { ownership ->
                ownership.nickname.normalized() == observedNickname.normalized()
            }

        if (
            observedNicknameOwner != null &&
            observedNicknameOwner.playerId != current.id
        ) {
            closeOpenHistory(
                playerId = observedNicknameOwner.playerId,
                expectedNickname = observedNicknameOwner.nickname,
                observedAt = observation.presence.observedAt,
            )
        }

        if (
            currentOwnership != null &&
            currentOwnership.nickname.normalized() != observedNickname.normalized()
        ) {
            nicknameOwnershipRepository.delete(currentOwnership)
        }

        synchronizePlayerHistory(
            playerId = current.id,
            observedNickname = observedNickname,
            observation = observation,
        )

        nicknameOwnershipRepository.save(
            PlayerNicknameOwnership(
                playerId = current.id,
                nickname = observedNickname,
                externalVersion = observation.externalVersion,
                observedAt = observation.presence.observedAt,
            ),
        )
    }

    private fun synchronizePlayerHistory(
        playerId: CristalixPlayerId,
        observedNickname: PlayerNickname,
        observation: PlayerObservation,
    ) {
        val openHistory = nicknameHistoryRepository.findOpenByPlayerIdForUpdate(playerId)
        val nicknameChanged =
            openHistory == null ||
                openHistory.nickname.normalized() != observedNickname.normalized()

        if (!nicknameChanged) {
            return
        }

        if (openHistory != null) {
            nicknameHistoryRepository.save(openHistory.closeAt(observation.presence.observedAt))
        }

        nicknameHistoryRepository.save(
            PlayerNicknameHistoryEntry(
                playerId = playerId,
                nickname = observedNickname,
                externalVersion = observation.externalVersion,
                validFrom = observation.presence.observedAt,
            ),
        )
    }

    private fun closeOpenHistory(
        playerId: CristalixPlayerId,
        expectedNickname: PlayerNickname,
        observedAt: Instant,
    ) {
        val history = nicknameHistoryRepository.findOpenByPlayerIdForUpdate(playerId) ?: return

        if (history.nickname.normalized() != expectedNickname.normalized()) {
            return
        }

        nicknameHistoryRepository.save(history.closeAt(observedAt))
    }
}
