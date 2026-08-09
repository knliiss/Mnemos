package com.mnemos.wallet.application.transaction

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.wallet.domain.model.request.TransactionRequestStatus
import com.mnemos.wallet.domain.repository.TransactionRequestRepository
import org.springframework.stereotype.Service

@Service
class ProcessCreateTransactionCommandService(
    private val transactionRequestRepository: TransactionRequestRepository,
    private val authorizationService: WalletTransactionAuthorizationService,
    private val createTransactionService: CreateTransactionService,
) {
    fun process(
        request: CreateTransactionRequest,
        metadata: EventMetadata,
    ): CreateTransactionResult {
        val storedStatus =
            transactionRequestRepository.findStatusByRequestId(
                request.requestId,
            )

        val shouldAuthorize =
            storedStatus == null || storedStatus == TransactionRequestStatus.PENDING

        val authorizationFailureReason =
            if (shouldAuthorize && request.amount > 0) {
                authorizationService.resolveFailureReason(
                    request = request,
                    actorUserId = metadata.actorUserId,
                )
            } else {
                null
            }

        return createTransactionService.create(
            request = request,
            metadata = metadata,
            preconditionFailureReason = authorizationFailureReason,
        )
    }
}
