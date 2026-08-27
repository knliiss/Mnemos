package com.mnemos.event.adapter.out.persistence.playeridentity

import com.mnemos.event.application.playeridentity.model.PlayerIdentityObservation
import com.mnemos.event.application.playeridentity.model.normalizeNickname
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class PlayerIdentityProjectionRepositoryAdapter(
    private val playerRepository: PlayerIdentityProjectionJpaRepository,
    private val ownerRepository: CurrentNicknameOwnerJpaRepository,
) : PlayerIdentityProjectionRepository {
    @Transactional
    override fun apply(observation: PlayerIdentityObservation) {
        val current = playerRepository.findByPlayerIdForUpdate(observation.playerId)

        if (current?.isNewerThan(observation.externalVersion, observation.observedAt) == true) {
            return
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
    }

    override fun findCurrentOwnerId(nickname: String): UUID? =
        ownerRepository.findById(normalizeNickname(nickname)).orElse(null)?.playerId

    override fun findCurrentNickname(playerId: UUID): String? =
        playerRepository.findById(playerId).orElse(null)?.nickname
}
