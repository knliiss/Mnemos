package com.mnemos.bet.adapter.out.persistence.itemrace

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ItemRaceParticipantJpaRepository : JpaRepository<ItemRaceParticipantEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ItemRaceParticipantEntity p WHERE p.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): ItemRaceParticipantEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ItemRaceParticipantEntity p WHERE p.raceId = :raceId AND p.accountId = :accountId")
    fun findByRaceAndAccountForUpdate(
        @Param("raceId") raceId: UUID,
        @Param("accountId") accountId: UUID,
    ): ItemRaceParticipantEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ItemRaceParticipantEntity p WHERE p.raceId = :raceId ORDER BY p.id")
    fun findByRaceForUpdate(
        @Param("raceId") raceId: UUID,
    ): List<ItemRaceParticipantEntity>

    fun findByRaceIdAndAccountId(
        raceId: UUID,
        accountId: UUID,
    ): ItemRaceParticipantEntity?

    fun findAllByRaceIdOrderById(raceId: UUID): List<ItemRaceParticipantEntity>

    fun findAllByAccountIdOrderByIdDesc(accountId: UUID): List<ItemRaceParticipantEntity>
}
