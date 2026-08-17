package com.mnemos.moderation.adapter.out.persistence.wallet

import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface WalletAdjustmentOperationJpaRepository : JpaRepository<WalletAdjustmentOperationEntity, UUID> {
    fun findByIdempotencyKey(idempotencyKey: UUID): WalletAdjustmentOperationEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT operation FROM WalletAdjustmentOperationEntity operation WHERE operation.requestId = :requestId")
    fun findByRequestIdForUpdate(
        @Param("requestId") requestId: UUID,
    ): WalletAdjustmentOperationEntity?

    fun findAllByTargetTypeAndTargetIdOrderByCreatedAtDesc(
        targetType: WalletOwnerType,
        targetId: UUID,
    ): List<WalletAdjustmentOperationEntity>
}
