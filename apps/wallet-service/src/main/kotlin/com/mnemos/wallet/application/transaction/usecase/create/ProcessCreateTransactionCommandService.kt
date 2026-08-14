package com.mnemos.wallet.application.transaction.usecase.create

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.wallet.application.transaction.policy.WalletCommandProducerPolicy
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import org.springframework.stereotype.Service

@Service
class ProcessCreateTransactionCommandService(
    private val producerPolicy: WalletCommandProducerPolicy,
    private val authorizationService: WalletTransactionAuthorizationService,
    private val createTransactionService: CreateTransactionService,
) {
    fun process(
        command: WalletCreateTransactionCommand,
        request: CreateTransactionRequest,
        producer: String,
        metadata: EventMetadata,
    ): CreateTransactionResult {
        producerPolicy.requireAllowed(
            producer = producer,
            command = command,
            actorUserId = metadata.actorUserId,
        )

        val authorizationFailureReason =
            if (request.amount > 0) {
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
