package com.mnemos.event.adapter.out.persistence.playeridentity

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PlayerIdentityProjectionJpaRepository : JpaRepository<PlayerIdentityProjectionEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT projection FROM PlayerIdentityProjectionEntity projection " +
            "WHERE projection.playerId = :playerId"
    )
    fun findByPlayerIdForUpdate(
        @Param("playerId") playerId: UUID,
    ): PlayerIdentityProjectionEntity?
}
