package com.mnemos.bet.application.wallet.usecase.failed

import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListFailedWalletOperationsService(
    private val repository: WalletOperationRepository,
) : ListFailedWalletOperationsUseCase {
    override fun execute(): List<FailedWalletOperationView> =
        repository
            .findFailedCredits()
            .map { operation ->
                FailedWalletOperationView(
                    id = operation.id,
                    accountId = operation.accountId,
                    amount = operation.amount,
                    kind = operation.kind,
                    eventBetId = operation.eventBetId,
                    eventBetResolutionId = operation.eventBetResolutionId,
                    raceParticipantId = operation.raceParticipantId,
                    createdAt = operation.createdAt,
                )
            }
}
