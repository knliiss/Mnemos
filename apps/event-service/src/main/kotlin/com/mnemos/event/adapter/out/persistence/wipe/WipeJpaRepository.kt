package com.mnemos.event.adapter.out.persistence.wipe

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface WipeJpaRepository : JpaRepository<WipeEntity, UUID> {
    @Query(
        """
        select wipe
        from WipeEntity wipe
        where wipe.startedAt <= :at
          and (wipe.endedAt is null or wipe.endedAt > :at)
        order by wipe.startedAt desc
        """,
    )
    fun findCurrentAt(
        @Param("at") at: Instant,
    ): List<WipeEntity>

    fun findAllByOrderByStartedAtDesc(): List<WipeEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select wipe
        from WipeEntity wipe
        where wipe.startedAt <= :at
          and (wipe.endedAt is null or wipe.endedAt > :at)
        order by wipe.startedAt desc
        """,
    )
    fun findCurrentForUpdate(
        @Param("at") at: Instant,
    ): List<WipeEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wipe from WipeEntity wipe order by wipe.startedAt desc")
    fun findLatestForUpdate(): List<WipeEntity>
}
