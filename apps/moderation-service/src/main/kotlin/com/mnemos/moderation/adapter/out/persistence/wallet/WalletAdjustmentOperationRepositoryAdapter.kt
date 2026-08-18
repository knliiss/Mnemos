package com.mnemos.moderation.adapter.out.persistence.wallet

import com.mnemos.moderation.adapter.out.persistence.wallet.WalletAdjustmentOperationPersistenceMapper.toDomain
import com.mnemos.moderation.adapter.out.persistence.wallet.WalletAdjustmentOperationPersistenceMapper.toEntity
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperationId
import com.mnemos.moderation.domain.wallet.repository.WalletAdjustmentOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class WalletAdjustmentOperationRepositoryAdapter(
    private val repository: WalletAdjustmentOperationJpaRepository,
) : WalletAdjustmentOperationRepository {
    override fun save(operation: WalletAdjustmentOperation): WalletAdjustmentOperation = repository.save(operation.toEntity()).toDomain()

    override fun findById(id: WalletAdjustmentOperationId): WalletAdjustmentOperation? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdempotencyKey(idempotencyKey: UUID): WalletAdjustmentOperation? =
        repository.findByIdempotencyKey(idempotencyKey)?.toDomain()

    override fun findByRequestIdForUpdate(requestId: UUID): WalletAdjustmentOperation? =
        repository.findByRequestIdForUpdate(requestId)?.toDomain()

    override fun findByTarget(
        targetType: WalletOwnerType,
        targetId: UUID,
    ): List<WalletAdjustmentOperation> =
        repository
            .findAllByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                targetType = targetType,
                targetId = targetId,
            ).map { entity -> entity.toDomain() }
}
