package com.mnemos.playerdirectory.adapter.out.persistence.player

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PlayerJpaRepository : JpaRepository<PlayerEntity, UUID> {
    fun findByNormalizedNickname(normalizedNickname: String): PlayerEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT player FROM PlayerEntity player WHERE player.playerId = :playerId")
    fun findByPlayerIdForUpdate(
        @Param("playerId") playerId: UUID,
    ): PlayerEntity?
}
