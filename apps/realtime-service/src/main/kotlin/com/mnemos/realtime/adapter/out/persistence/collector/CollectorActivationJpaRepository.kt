package com.mnemos.realtime.adapter.out.persistence.collector

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CollectorActivationJpaRepository : JpaRepository<CollectorActivationEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT activation FROM CollectorActivationEntity activation WHERE activation.tokenHash = :tokenHash")
    fun findByTokenHashForUpdate(
        @Param("tokenHash")
        tokenHash: String,
    ): CollectorActivationEntity?
}
