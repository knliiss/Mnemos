package com.mnemos.event.adapter.out.persistence.playeridentity

import com.mnemos.event.adapter.out.persistence.booster.BoosterEventJpaRepository
import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventJpaRepository
import com.mnemos.event.application.playeridentity.model.normalizeNickname
import com.mnemos.event.application.playeridentity.port.out.PendingEventIdentityRepository
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Repository
class PendingEventIdentityRepositoryAdapter(
    private val itemDropRepository: ItemDropEventJpaRepository,
    private val boosterRepository: BoosterEventJpaRepository,
) : PendingEventIdentityRepository {
    @Transactional
    override fun reconcile(
        nickname: String,
        playerId: CristalixPlayerId,
        observedAt: Instant,
        maxAge: Duration,
    ) {
        val normalizedNickname = normalizeNickname(nickname)
        val cutoff = observedAt.minus(maxAge)

        itemDropRepository.expirePendingIdentity(
            normalizedNickname = normalizedNickname,
            cutoff = cutoff,
        )
        boosterRepository.expirePendingIdentity(
            normalizedNickname = normalizedNickname,
            cutoff = cutoff,
        )

        itemDropRepository.resolvePendingIdentity(
            normalizedNickname = normalizedNickname,
            playerId = playerId.id,
            cutoff = cutoff,
        )
        boosterRepository.resolvePendingIdentity(
            normalizedNickname = normalizedNickname,
            playerId = playerId.id,
            cutoff = cutoff,
        )
    }
}
