package com.mnemos.bet.application.wallet.usecase.retry

import com.mnemos.bet.application.wallet.port.out.BetWallet
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RetryWalletOperationService(
    private val walletOperationRepository: WalletOperationRepository,
    private val eventBetRepository: EventBetRepository,
    private val itemRaceRepository: ItemRaceRepository,
    private val wallet: BetWallet,
    private val clock: Clock,
) : RetryWalletOperationUseCase {
    @Transactional
    override fun execute(command: RetryWalletOperationCommand) {
        val failedOperation =
            walletOperationRepository.findByIdForUpdate(command.operationId)
                ?: throw BetNotFoundException("Wallet operation ${command.operationId} was not found")

        if (failedOperation.status != WalletOperationStatus.FAILED) {
            throw BetConflictException("Only failed wallet operations can be retried")
        }

        if (failedOperation.kind == WalletOperationKind.STAKE) {
            throw BetConflictException("Failed stake operations cannot be retried administratively")
        }

        prepareAggregateForRetry(failedOperation)

        val retryOperation = failedOperation.retry(clock.instant())

        walletOperationRepository.create(retryOperation)
        wallet.request(retryOperation)
    }

    private fun prepareAggregateForRetry(operation: WalletOperation) {
        operation.eventBetId?.let { betId ->
            val bet =
                eventBetRepository.findBetByIdForUpdate(betId)
                    ?: throw BetNotFoundException("Event bet $betId was not found")

            when (operation.kind) {
                WalletOperationKind.PAYOUT -> bet.preparePayout()
                WalletOperationKind.REFUND -> if (operation.eventBetResolutionId != null) bet.prepareRefund()
                WalletOperationKind.STAKE -> Unit
            }

            eventBetRepository.saveBet(bet)
        }

        operation.raceParticipantId?.let { participantId ->
            val participant =
                itemRaceRepository.findParticipantByIdForUpdate(participantId)
                    ?: throw BetNotFoundException("Item race participant $participantId was not found")

            when (operation.kind) {
                WalletOperationKind.PAYOUT -> participant.preparePayout()
                WalletOperationKind.REFUND -> participant.prepareRefund()
                WalletOperationKind.STAKE -> Unit
            }

            itemRaceRepository.saveParticipant(participant)
        }
    }
}
