package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface EventBetRoundJpaRepository : JpaRepository<EventBetRoundEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM EventBetRoundEntity r WHERE r.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): EventBetRoundEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT r FROM EventBetRoundEntity r " +
            "WHERE r.eventType = :eventType AND r.scheduledAt = :scheduledAt",
    )
    fun findByEventAndScheduleForUpdate(
        @Param("eventType") eventType: DomainBetGlobalEventType,
        @Param("scheduledAt") scheduledAt: Instant,
    ): EventBetRoundEntity?

    @Query(
        "SELECT r FROM EventBetRoundEntity r " +
            "WHERE r.status <> :settledStatus " +
            "AND (r.closesAt <= :now OR r.resolvesAt <= :now) " +
            "ORDER BY r.scheduledAt ASC, r.eventType ASC, r.id ASC",
    )
    fun findRequiringSynchronization(
        @Param("now") now: Instant,
        @Param("settledStatus") settledStatus: EventBetRoundStatus,
    ): List<EventBetRoundEntity>

    @Query(
        "SELECT r FROM EventBetRoundEntity r " +
            "WHERE r.status <> :settledStatus " +
            "ORDER BY r.scheduledAt ASC, r.eventType ASC, r.id ASC",
    )
    fun findCurrent(
        @Param("settledStatus") settledStatus: EventBetRoundStatus,
    ): List<EventBetRoundEntity>
}
