package com.mnemos.event.adapter.out.persistence.wipe

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface WipeJpaRepository : JpaRepository<WipeEntity, UUID> {
    fun findFirstByEndedAtIsNullOrderByStartedAtDesc(): WipeEntity?

    fun findAllByOrderByStartedAtDesc(): List<WipeEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wipe from WipeEntity wipe where wipe.endedAt is null order by wipe.startedAt desc")
    fun findCurrentForUpdate(): List<WipeEntity>
}
