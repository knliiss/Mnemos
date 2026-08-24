package com.mnemos.wallet.application.transaction.usecase.create

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.wallet.application.transaction.event.WalletTransactionIntegrationEvents
import com.mnemos.wallet.application.transaction.mapper.CreateTransactionResponseMapper.toCompletedResponse
import com.mnemos.wallet.application.transaction.mapper.CreateTransactionResponseMapper.toResponse
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.balance.repository.BalanceRepository
import com.mnemos.wallet.domain.exception.BalanceLimitExceededException
import com.mnemos.wallet.domain.exception.InsufficientBalanceException
import com.mnemos.wallet.domain.transaction.model.Transaction
import com.mnemos.wallet.domain.transaction.model.TransactionAmount
import com.mnemos.wallet.domain.transaction.model.TransactionRequest
import com.mnemos.wallet.domain.transaction.model.TransactionRequestStatus
import com.mnemos.wallet.domain.transaction.repository.TransactionRepository
import com.mnemos.wallet.domain.transaction.repository.TransactionRequestRepository
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CreateTransactionService(
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionRequestRepository: TransactionRequestRepository,
    private val integrationEventPublisher: IntegrationEventPublisher,
) {
    @Transactional
    fun create(
        request: CreateTransactionRequest,
        metadata: EventMetadata,
        preconditionFailureReason: WalletTransactionFailureReason? = null,
    ): CreateTransactionResult {
        val now = Instant.now()

        transactionRequestRepository.createIfAbsent(
            TransactionRequest.create(
                requestId = request.requestId,
                owner = request.owner,
                currency = request.currency,
                type = request.type,
                source = request.source,
                sourceReferenceId = request.sourceReferenceId,
                amount = request.amount,
                createdAt = now,
            ),
        )

        val storedRequest =
            requireNotNull(transactionRequestRepository.findByRequestIdForUpdate(request.requestId)) {
                "Transaction request ${request.requestId.id} was not found after initialization."
            }

        if (!storedRequest.matches(
                owner = request.owner,
                currency = request.currency,
                type = request.type,
                source = request.source,
                sourceReferenceId = request.sourceReferenceId,
                amount = request.amount,
            )
        ) {
            return CreateTransactionResult.Failed(
                requestId = storedRequest.requestId,
                reason = WalletTransactionFailureReason.IDEMPOTENCY_CONFLICT,
            )
        }

        return when (storedRequest.status) {
            TransactionRequestStatus.COMPLETED ->
                CreateTransactionResult.Completed(storedRequest.toCompletedResponse())

            TransactionRequestStatus.FAILED ->
                CreateTransactionResult.Failed(
                    requestId = storedRequest.requestId,
                    reason = requireNotNull(storedRequest.failureReason),
                )

            TransactionRequestStatus.PENDING ->
                processPendingRequest(
                    request = request,
                    storedRequest = storedRequest,
                    occurredAt = now,
                    metadata = metadata,
                    preconditionFailureReason = preconditionFailureReason,
                )
        }
    }

    private fun processPendingRequest(
        request: CreateTransactionRequest,
        storedRequest: TransactionRequest,
        occurredAt: Instant,
        metadata: EventMetadata,
        preconditionFailureReason: WalletTransactionFailureReason?,
    ): CreateTransactionResult {
        if (request.amount <= 0) {
            return failPending(
                request = storedRequest,
                reason = WalletTransactionFailureReason.INVALID_AMOUNT,
                occurredAt = occurredAt,
                metadata = metadata,
            )
        }

        if (!WalletCurrencyPolicy.supports(request.owner.type, request.currency)) {
            return failPending(
                request = storedRequest,
                reason = WalletTransactionFailureReason.OWNER_CURRENCY_MISMATCH,
                occurredAt = occurredAt,
                metadata = metadata,
            )
        }

        if (preconditionFailureReason != null) {
            return failPending(
                request = storedRequest,
                reason = preconditionFailureReason,
                occurredAt = occurredAt,
                metadata = metadata,
            )
        }

        ensureBalanceExists(request, occurredAt)

        val balance =
            requireNotNull(
                balanceRepository.findForUpdate(
                    owner = request.owner,
                    currency = request.currency,
                ),
            ) {
                "Balance ${request.owner.type}/${request.owner.id}/${request.currency} was not found after initialization."
            }

        val transaction =
            Transaction.create(
                owner = request.owner,
                currency = request.currency,
                type = request.type,
                source = request.source,
                sourceReferenceId = request.sourceReferenceId,
                amount = TransactionAmount(request.amount),
                requestId = request.requestId,
                createdAt = occurredAt,
            )

        try {
            balance.apply(transaction.toBalanceChange())
        } catch (_: InsufficientBalanceException) {
            return failPending(
                request = storedRequest,
                reason = WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
                occurredAt = occurredAt,
                metadata = metadata,
            )
        } catch (_: BalanceLimitExceededException) {
            return failPending(
                request = storedRequest,
                reason = WalletTransactionFailureReason.BALANCE_LIMIT_EXCEEDED,
                occurredAt = occurredAt,
                metadata = metadata,
            )
        }

        transactionRepository.create(transaction)
        balanceRepository.update(balance)

        storedRequest.complete(
            transactionId = transaction.id,
            completedAt = occurredAt,
        )
        transactionRequestRepository.update(storedRequest)

        return publishCompleted(
            response = transaction.toResponse(),
            occurredAt = occurredAt,
            metadata = metadata,
        )
    }

    private fun ensureBalanceExists(
        request: CreateTransactionRequest,
        occurredAt: Instant,
    ) {
        balanceRepository.createIfAbsent(
            Balance.create(
                owner = request.owner,
                currency = request.currency,
                createdAt = occurredAt,
            ),
        )
    }

    private fun failPending(
        request: TransactionRequest,
        reason: WalletTransactionFailureReason,
        occurredAt: Instant,
        metadata: EventMetadata,
    ): CreateTransactionResult.Failed {
        request.fail(
            reason = reason,
            completedAt = occurredAt,
        )
        transactionRequestRepository.update(request)

        return publishFailure(
            request = request,
            reason = reason,
            occurredAt = occurredAt,
            metadata = metadata,
        )
    }

    private fun publishCompleted(
        response: CreateTransactionResponse,
        occurredAt: Instant,
        metadata: EventMetadata,
    ): CreateTransactionResult.Completed {
        integrationEventPublisher.publish(
            event = WalletTransactionIntegrationEvents.completed(response, occurredAt),
            metadata = metadata,
        )

        return CreateTransactionResult.Completed(response)
    }

    private fun publishFailure(
        request: TransactionRequest,
        reason: WalletTransactionFailureReason,
        occurredAt: Instant,
        metadata: EventMetadata,
    ): CreateTransactionResult.Failed {
        integrationEventPublisher.publish(
            event = WalletTransactionIntegrationEvents.failed(request, reason, occurredAt),
            metadata = metadata,
        )

        return CreateTransactionResult.Failed(
            requestId = request.requestId,
            reason = reason,
        )
    }
}
