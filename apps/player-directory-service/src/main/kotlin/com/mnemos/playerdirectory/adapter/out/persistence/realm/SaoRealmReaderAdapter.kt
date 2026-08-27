package com.mnemos.playerdirectory.adapter.out.persistence.realm

import com.mnemos.playerdirectory.application.realm.model.SaoRealmPlayer
import com.mnemos.playerdirectory.application.realm.port.out.SaoRealmReader
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class SaoRealmReaderAdapter(
    private val repository: SaoRealmJpaRepository,
) : SaoRealmReader {
    override fun findFreshOnlinePlayers(cutoff: Instant): List<SaoRealmPlayer> =
        repository.findFreshOnlinePlayers(cutoff).map { projection ->
            SaoRealmPlayer(
                playerId = CristalixPlayerId(projection.playerId),
                nickname = projection.nickname,
                accountId = projection.accountId,
                realm = projection.realm,
                observedAt = projection.observedAt,
            )
        }
}
