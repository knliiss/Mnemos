package com.mnemos.wallet.application.transaction

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.wallet.application.balance.InitializeAccountBalancesService
import com.mnemos.wallet.application.event.WalletTransactionIntegrationEvents
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.exception.BalanceLimitExceededException
import com.mnemos.wallet.domain.exception.InsufficientBalanceException
import com.mnemos.wallet.domain.model.request.TransactionRequest
import com.mnemos.wallet.domain.model.request.TransactionRequestStatus
import com.mnemos.wallet.domain.model.transaction.Transaction
import com.mnemos.wallet.domain.model.transaction.TransactionAmount
import com.mnemos.wallet.domain.repository.BalanceRepository
import com.mnemos.wallet.domain.repository.TransactionRepository
import com.mnemos.wallet.domain.repository.TransactionRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CreateTransactionService(
    private val initializeAccountBalancesService: InitializeAccountBalancesService,
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
                accountId = request.accountId,
                currency = request.currency,
                type = request.type,
                source = request.source,
                sourceReferenceId = request.sourceReferenceId,
                amount = request.amount,
                createdAt = now,
            ),
        )

        val storedRequest =
            requireNotNull(
                transactionRequestRepository.findByRequestIdForUpdate(request.requestId),
            ) {
                "Transaction request ${request.requestId.id} was not found after initialization."
            }

        if (!storedRequest.matches(
                accountId = request.accountId,
                currency = request.currency,
                type = request.type,
                source = request.source,
                sourceReferenceId = request.sourceReferenceId,
                amount = request.amount,
            )
        ) {
            return publishFailure(
                request = storedRequest,
                reason = WalletTransactionFailureReason.IDEMPOTENCY_CONFLICT,
                occurredAt = now,
                metadata = metadata,
            )
        }

        return when (storedRequest.status) {
            TransactionRequestStatus.COMPLETED ->
                publishCompleted(
                    response = storedRequest.toCompletedResponse(),
                    occurredAt = storedRequest.completedAt ?: now,
                    metadata = metadata,
                )

            TransactionRequestStatus.FAILED ->
                publishFailure(
                    request = storedRequest,
                    reason = requireNotNull(storedRequest.failureReason),
                    occurredAt = storedRequest.completedAt ?: now,
                    metadata = metadata,
                )

            TransactionRequestStatus.PENDING ->
                preconditionFailureReason?.let { reason ->
                    failPending(
                        request = storedRequest,
                        reason = reason,
                        occurredAt = now,
                        metadata = metadata,
                    )
                } ?: processPending(
                    request = request,
                    storedRequest = storedRequest,
                    occurredAt = now,
                    metadata = metadata,
                )
        }
    }

    private fun processPending(
        request: CreateTransactionRequest,
        storedRequest: TransactionRequest,
        occurredAt: Instant,
        metadata: EventMetadata,
    ): CreateTransactionResult {
        if (request.amount <= 0) {
            return failPending(
                request = storedRequest,
                reason = WalletTransactionFailureReason.INVALID_AMOUNT,
                occurredAt = occurredAt,
                metadata = metadata,
            )
        }

        initializeAccountBalancesService.initialize(request.accountId)

        val balance =
            requireNotNull(
                balanceRepository.findByAccountIdAndCurrencyForUpdate(
                    accountId = request.accountId,
                    currency = request.currency,
                ),
            ) {
                "Balance ${request.accountId.id}/${request.currency} was not found after lazy initialization."
            }

        val transaction =
            Transaction.create(
                accountId = request.accountId,
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
            event =
                WalletTransactionIntegrationEvents.completed(
                    response = response,
                    occurredAt = occurredAt,
                ),
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
            event =
                WalletTransactionIntegrationEvents.failed(
                    request = request,
                    reason = reason,
                    occurredAt = occurredAt,
                ),
            metadata = metadata,
        )

        return CreateTransactionResult.Failed(
            requestId = request.requestId,
            reason = reason,
        )
    }

    private fun Transaction.toResponse(): CreateTransactionResponse =
        CreateTransactionResponse(
            transactionId = id,
            requestId = requestId,
            accountId = accountId,
            sourceReferenceId = sourceReferenceId,
        )

    private fun TransactionRequest.toCompletedResponse(): CreateTransactionResponse =
        CreateTransactionResponse(
            transactionId = requireNotNull(transactionId),
            requestId = requestId,
            accountId = accountId,
            sourceReferenceId = sourceReferenceId,
        )
}
