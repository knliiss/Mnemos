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

    @Query(
        """
        SELECT operation
        FROM WalletOperationEntity operation
        WHERE operation.accountId = :accountId
          AND operation.status = :status
        ORDER BY operation.createdAt ASC, operation.id ASC
        """,
    )
    fun findAllByAccountIdAndStatusOrderByCreatedAtAsc(
        @Param("accountId") accountId: UUID,
        @Param("status") status: WalletOperationStatus,
    ): List<WalletOperationEntity>

    @Query(
        """
        SELECT operation
        FROM WalletOperationEntity operation
        WHERE operation.status = :status
          AND operation.kind IN :kinds
        ORDER BY operation.createdAt ASC, operation.id ASC
        """,
    )
    fun findAllByStatusAndKindInOrderByCreatedAtAsc(
        @Param("status") status: WalletOperationStatus,
        @Param("kinds") kinds: Collection<WalletOperationKind>,
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
