package com.mnemos.playerdirectory.adapter.out.persistence.nickname

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PlayerNicknameHistoryJpaRepository : JpaRepository<PlayerNicknameHistoryEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT history
        FROM PlayerNicknameHistoryEntity history
        WHERE history.playerId = :playerId
          AND history.validUntil IS NULL
        """,
    )
    fun findOpenByPlayerIdForUpdate(
        @Param("playerId") playerId: UUID,
    ): PlayerNicknameHistoryEntity?
}
