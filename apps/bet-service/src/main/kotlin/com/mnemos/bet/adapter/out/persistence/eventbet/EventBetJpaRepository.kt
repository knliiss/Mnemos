package com.mnemos.bet.adapter.out.persistence.eventbet

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EventBetJpaRepository : JpaRepository<EventBetEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM EventBetEntity b WHERE b.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): EventBetEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM EventBetEntity b WHERE b.roundId = :roundId AND b.accountId = :accountId")
    fun findByRoundAndAccountForUpdate(
        @Param("roundId") roundId: UUID,
        @Param("accountId") accountId: UUID,
    ): EventBetEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM EventBetEntity b WHERE b.roundId = :roundId ORDER BY b.id")
    fun findByRoundForUpdate(
        @Param("roundId") roundId: UUID,
    ): List<EventBetEntity>

    fun findByRoundIdAndAccountId(
        roundId: UUID,
        accountId: UUID,
    ): EventBetEntity?

    fun findAllByRoundIdInAndAccountId(
        roundIds: Collection<UUID>,
        accountId: UUID,
    ): List<EventBetEntity>

    fun findAllByRoundIdOrderByIdAsc(roundId: UUID): List<EventBetEntity>

    fun findAllByAccountIdOrderByIdDesc(accountId: UUID): List<EventBetEntity>
}
