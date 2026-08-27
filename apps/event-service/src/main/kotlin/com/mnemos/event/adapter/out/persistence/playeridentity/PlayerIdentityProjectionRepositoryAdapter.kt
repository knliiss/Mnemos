package com.mnemos.event.adapter.out.persistence.playeridentity

import com.mnemos.event.application.playeridentity.model.PlayerIdentityObservation
import com.mnemos.event.application.playeridentity.model.normalizeNickname
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class PlayerIdentityProjectionRepositoryAdapter(
    private val playerRepository: PlayerIdentityProjectionJpaRepository,
    private val ownerRepository: CurrentNicknameOwnerJpaRepository,
) : PlayerIdentityProjectionRepository {
    @Transactional
    override fun apply(observation: PlayerIdentityObservation): Boolean {
        val current = playerRepository.findByPlayerIdForUpdate(observation.playerId)

        if (current?.isNewerThan(observation.externalVersion, observation.observedAt) == true) {
            return false
        }

        val previousNormalizedNickname = current?.normalizedNickname

        if (previousNormalizedNickname != null && previousNormalizedNickname != observation.normalizedNickname) {
            ownerRepository.deleteOwnedNicknameIfNotNewer(
                normalizedNickname = previousNormalizedNickname,
                playerId = observation.playerId,
                externalVersion = observation.externalVersion,
                observedAt = observation.observedAt,
            )
        }

        if (current == null) {
            playerRepository.save(
                PlayerIdentityProjectionEntity(
                    playerId = observation.playerId,
                    nickname = observation.nickname,
                    normalizedNickname = observation.normalizedNickname,
                    externalVersion = observation.externalVersion,
                    observedAt = observation.observedAt,
                ),
            )
        } else {
            current.update(
                nickname = observation.nickname,
                normalizedNickname = observation.normalizedNickname,
                externalVersion = observation.externalVersion,
                observedAt = observation.observedAt,
            )
            playerRepository.save(current)
        }

        ownerRepository.upsertIfNotOlder(
            normalizedNickname = observation.normalizedNickname,
            nickname = observation.nickname,
            playerId = observation.playerId,
            externalVersion = observation.externalVersion,
            observedAt = observation.observedAt,
        )

        return true
    }

    override fun findCurrentOwnerId(nickname: String): CristalixPlayerId? =
        ownerRepository
            .findById(normalizeNickname(nickname))
            .orElse(null)
            ?.playerId
            ?.let(::CristalixPlayerId)

    override fun findCurrentNickname(playerId: CristalixPlayerId): String? =
        playerRepository.findById(playerId.id).orElse(null)?.nickname
}
