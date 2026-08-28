package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

interface EventBetRoundJpaRepository : JpaRepository<EventBetRoundEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO event_bet_rounds (
                id,
                event_type,
                scheduled_at,
                closes_at,
                resolves_at,
                yes_odds,
                no_odds,
                status
            )
            VALUES (
                :id,
                :eventType,
                :scheduledAt,
                :closesAt,
                :resolvesAt,
                :yesOdds,
                :noOdds,
                :status
            )
            ON CONFLICT (event_type, scheduled_at) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("id") id: UUID,
        @Param("eventType") eventType: String,
        @Param("scheduledAt") scheduledAt: Instant,
        @Param("closesAt") closesAt: Instant,
        @Param("resolvesAt") resolvesAt: Instant,
        @Param("yesOdds") yesOdds: BigDecimal,
        @Param("noOdds") noOdds: BigDecimal,
        @Param("status") status: String,
    ): Int

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
            "ORDER BY r.scheduledAt ASC, r.id ASC",
    )
    fun findRequiringSynchronization(
        @Param("now") now: Instant,
        @Param("settledStatus") settledStatus: EventBetRoundStatus,
    ): List<EventBetRoundEntity>

    @Query(
        "SELECT r FROM EventBetRoundEntity r " +
            "WHERE r.status <> :settledStatus " +
            "ORDER BY r.scheduledAt ASC, r.id ASC",
    )
    fun findCurrent(
        @Param("settledStatus") settledStatus: EventBetRoundStatus,
    ): List<EventBetRoundEntity>

    fun findTop200ByOrderByScheduledAtDescIdDesc(): List<EventBetRoundEntity>
}
