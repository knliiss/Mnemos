package com.mnemos.wallet.infrastructure.persistence.request

import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.model.request.TransactionRequestStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface TransactionRequestJpaRepository : JpaRepository<TransactionRequestEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT request
        FROM TransactionRequestEntity request
        WHERE request.requestId = :requestId
        """,
    )
    fun findByRequestIdForUpdate(
        @Param("requestId") requestId: UUID,
    ): TransactionRequestEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE TransactionRequestEntity request
        SET request.status = :status,
            request.transactionId = :transactionId,
            request.failureReason = :failureReason,
            request.completedAt = :completedAt
        WHERE request.requestId = :requestId
        """,
    )
    fun updateResult(
        @Param("requestId") requestId: UUID,
        @Param("status") status: TransactionRequestStatus,
        @Param("transactionId") transactionId: UUID?,
        @Param("failureReason") failureReason: WalletTransactionFailureReason?,
        @Param("completedAt") completedAt: Instant?,
    ): Int

    @Query(
        """
            SELECT request.status
            FROM TransactionRequestEntity request
            WHERE request.requestId = :requestId
        """,
    )
    fun findStatusByRequestId(
        @Param("requestId") requestId: UUID,
    ): TransactionRequestStatus?
}
