package com.mnemos.playerdirectory.adapter.out.persistence.accountlink

import com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink
import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import org.springframework.stereotype.Repository

@Repository
class PlayerAccountLinkRepositoryAdapter(
    private val repository: PlayerAccountLinkJpaRepository,
) : PlayerAccountLinkRepository {
    override fun save(link: PlayerAccountLink) {
        repository.save(
            PlayerAccountLinkEntity(
                playerId = link.playerId.id,
                accountId = link.accountId,
                recordedAt = link.recordedAt,
            ),
        )
    }
}
