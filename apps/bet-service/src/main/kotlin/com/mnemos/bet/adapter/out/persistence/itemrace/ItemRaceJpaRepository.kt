package com.mnemos.bet.adapter.out.persistence.itemrace

import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ItemRaceJpaRepository : JpaRepository<ItemRaceEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ItemRaceEntity r WHERE r.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): ItemRaceEntity?

    @Query(
        "SELECT r FROM ItemRaceEntity r " +
            "WHERE r.status IN :synchronizingStatuses " +
            "AND (r.startsAt <= :now OR r.expiresAt <= :now OR r.status = :refundingStatus) " +
            "ORDER BY r.startsAt ASC, r.id ASC",
    )
    fun findRequiringSynchronization(
        @Param("now") now: Instant,
        @Param("synchronizingStatuses") synchronizingStatuses: Collection<ItemRaceStatus>,
        @Param("refundingStatus") refundingStatus: ItemRaceStatus,
    ): List<ItemRaceEntity>

    @Query(
        "SELECT r FROM ItemRaceEntity r " +
            "WHERE r.status IN :currentStatuses " +
            "ORDER BY r.startsAt ASC, r.id ASC",
    )
    fun findCurrent(
        @Param("currentStatuses") currentStatuses: Collection<ItemRaceStatus>,
    ): List<ItemRaceEntity>

    fun findTop200ByOrderByStartsAtDescIdDesc(): List<ItemRaceEntity>
}
