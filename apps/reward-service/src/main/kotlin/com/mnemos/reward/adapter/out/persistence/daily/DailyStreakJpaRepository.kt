package com.mnemos.reward.adapter.out.persistence.daily

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface DailyStreakJpaRepository : JpaRepository<DailyStreakEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DailyStreakEntity s where s.accountId = :accountId")
    fun findForUpdate(
        @Param("accountId") accountId: UUID,
    ): DailyStreakEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DailyStreakEntity s where s.currentStreak > 0 and s.lastClaimedDate < :cutoff")
    fun findMissedForUpdate(
        @Param("cutoff") cutoff: LocalDate,
    ): List<DailyStreakEntity>
}
