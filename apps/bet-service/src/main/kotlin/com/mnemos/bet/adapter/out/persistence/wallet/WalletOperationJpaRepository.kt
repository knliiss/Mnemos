package com.mnemos.bet.adapter.out.persistence.wallet

import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface WalletOperationJpaRepository : JpaRepository<WalletOperationEntity, UUID> {
    fun findByIdempotencyKey(idempotencyKey: UUID): WalletOperationEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM WalletOperationEntity o WHERE o.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): WalletOperationEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM WalletOperationEntity o WHERE o.requestId = :requestId")
    fun findByRequestIdForUpdate(
        @Param("requestId") requestId: UUID,
    ): WalletOperationEntity?

    fun findAllByAccountIdAndStatusOrderByCreatedAtAsc(
        accountId: UUID,
        status: WalletOperationStatus,
    ): List<WalletOperationEntity>

    fun findAllByStatusAndKindInOrderByCreatedAtAsc(
        status: WalletOperationStatus,
        kinds: Collection<WalletOperationKind>,
    ): List<WalletOperationEntity>

    fun existsByEventBetIdAndKindAndStatusIn(
        eventBetId: UUID,
        kind: WalletOperationKind,
        statuses: Collection<WalletOperationStatus>,
    ): Boolean

    fun existsByRaceParticipantIdAndKindAndStatusIn(
        raceParticipantId: UUID,
        kind: WalletOperationKind,
        statuses: Collection<WalletOperationStatus>,
    ): Boolean
}
