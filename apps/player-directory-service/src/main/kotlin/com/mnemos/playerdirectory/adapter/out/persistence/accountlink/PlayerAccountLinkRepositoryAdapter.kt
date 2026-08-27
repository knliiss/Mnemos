package com.mnemos.playerdirectory.adapter.out.persistence.accountlink

import com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink
import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class PlayerAccountLinkRepositoryAdapter(
    private val repository: PlayerAccountLinkJpaRepository,
) : PlayerAccountLinkRepository {
    @Transactional
    override fun save(link: PlayerAccountLink) {
        val updatedRows =
            repository.upsert(
                playerId = link.playerId.id,
                accountId = link.accountId,
                recordedAt = link.recordedAt,
            )

        check(updatedRows == 1) {
            "Player ${link.playerId.id} is already linked to another account"
        }
    }
}
