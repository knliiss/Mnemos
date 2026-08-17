package com.mnemos.bet.adapter.out.persistence.wallet

import com.mnemos.bet.adapter.out.persistence.wallet.WalletOperationPersistenceMapper.toDomain
import com.mnemos.bet.adapter.out.persistence.wallet.WalletOperationPersistenceMapper.toEntity
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationId
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import com.mnemos.bet.domain.wallet.model.WalletRequestId
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class WalletOperationRepositoryAdapter(
    private val jpaRepository: WalletOperationJpaRepository,
) : WalletOperationRepository {
    override fun create(operation: WalletOperation): WalletOperation = jpaRepository.save(operation.toEntity()).toDomain()

    override fun save(operation: WalletOperation): WalletOperation = jpaRepository.save(operation.toEntity()).toDomain()

    override fun findById(operationId: WalletOperationId): WalletOperation? =
        jpaRepository.findById(operationId.id).orElse(null)?.toDomain()

    override fun findByIdempotencyKey(idempotencyKey: UUID): WalletOperation? =
        jpaRepository.findByIdempotencyKey(idempotencyKey)?.toDomain()

    override fun findByIdForUpdate(operationId: WalletOperationId): WalletOperation? =
        jpaRepository.findByIdForUpdate(operationId.id)?.toDomain()

    override fun findByRequestIdForUpdate(requestId: WalletRequestId): WalletOperation? =
        jpaRepository.findByRequestIdForUpdate(requestId.id)?.toDomain()

    override fun findPendingByAccountId(accountId: AccountId): List<WalletOperation> =
        jpaRepository
            .findAllByAccountIdAndStatusOrderByCreatedAtAsc(
                accountId = accountId.id,
                status = WalletOperationStatus.PENDING,
            ).map { entity -> entity.toDomain() }

    override fun findFailedCredits(): List<WalletOperation> =
        jpaRepository
            .findAllByStatusAndKindInOrderByCreatedAtAsc(
                status = WalletOperationStatus.FAILED,
                kinds = CREDIT_KINDS,
            ).map { entity -> entity.toDomain() }

    override fun existsForEventBet(
        eventBetId: EventBetId,
        kind: WalletOperationKind,
    ): Boolean =
        jpaRepository.existsByEventBetIdAndKindAndStatusIn(
            eventBetId = eventBetId.id,
            kind = kind,
            statuses = ACTIVE_OPERATION_STATUSES,
        )

    override fun existsForRaceParticipant(
        participantId: ItemRaceParticipantId,
        kind: WalletOperationKind,
    ): Boolean =
        jpaRepository.existsByRaceParticipantIdAndKindAndStatusIn(
            raceParticipantId = participantId.id,
            kind = kind,
            statuses = ACTIVE_OPERATION_STATUSES,
        )

    private companion object {
        val CREDIT_KINDS = listOf(WalletOperationKind.PAYOUT, WalletOperationKind.REFUND)
        val ACTIVE_OPERATION_STATUSES = listOf(WalletOperationStatus.PENDING, WalletOperationStatus.COMPLETED)
    }
}
