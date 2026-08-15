package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.application.wallet.event.BetOperationEventPublisher
import com.mnemos.bet.contracts.shared.BetOperationStatus
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessWalletTransactionFailedService(
    private val walletOperationRepository: WalletOperationRepository,
    private val eventBetRepository: EventBetRepository,
    private val itemRaceRepository: ItemRaceRepository,
    private val operationEventPublisher: BetOperationEventPublisher,
) : ProcessWalletTransactionFailedUseCase {
    @Transactional
    override fun execute(command: ProcessWalletTransactionFailedCommand) {
        val operation =
            walletOperationRepository.findByRequestIdForUpdate(command.requestId)
                ?: return

        if (operation.status != WalletOperationStatus.PENDING) {
            return
        }

        operation.fail()
        walletOperationRepository.save(operation)

        operation.eventBetId?.let { betId ->
            val bet = eventBetRepository.findBetByIdForUpdate(betId) ?: return@let

            when (operation.kind) {
                WalletOperationKind.STAKE -> bet.failStake(operation.amount)
                WalletOperationKind.PAYOUT -> bet.failPayout()
                WalletOperationKind.REFUND -> if (operation.eventBetResolutionId != null) bet.failRefund()
            }

            eventBetRepository.saveBet(bet)
        }

        operation.raceParticipantId?.let { participantId ->
            val participant = itemRaceRepository.findParticipantByIdForUpdate(participantId) ?: return@let

            when (operation.kind) {
                WalletOperationKind.STAKE -> participant.failEntry(operation.amount)
                WalletOperationKind.PAYOUT -> participant.failPayout()
                WalletOperationKind.REFUND -> participant.failRefund()
            }

            itemRaceRepository.saveParticipant(participant)
        }

        operationEventPublisher.publish(
            operation = operation,
            status = BetOperationStatus.FAILED,
            failureReason = command.reason.name,
        )
    }
}
