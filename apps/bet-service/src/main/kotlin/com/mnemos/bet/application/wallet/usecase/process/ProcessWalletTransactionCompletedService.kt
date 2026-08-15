package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.application.wallet.event.BetOperationEventPublisher
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletCreditCommand
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.contracts.shared.BetOperationStatus
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ProcessWalletTransactionCompletedService(
    private val walletOperationRepository: WalletOperationRepository,
    private val eventBetRepository: EventBetRepository,
    private val itemRaceRepository: ItemRaceRepository,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val operationEventPublisher: BetOperationEventPublisher,
    private val clock: Clock,
) : ProcessWalletTransactionCompletedUseCase {
    @Transactional
    override fun execute(command: ProcessWalletTransactionCompletedCommand) {
        val operation =
            walletOperationRepository.findByRequestIdForUpdate(command.requestId)
                ?: return

        if (operation.status != WalletOperationStatus.PENDING) {
            return
        }

        operation.complete(command.transactionId)
        walletOperationRepository.save(operation)

        val outcome =
            when {
                operation.eventBetId != null -> processEventBet(operation)
                operation.raceParticipantId != null -> processItemRace(operation)
                else -> OperationOutcome.failed("RESOURCE_NOT_FOUND")
            }

        operationEventPublisher.publish(
            operation = operation,
            status = outcome.status,
            failureReason = outcome.failureReason,
        )
    }

    private fun processEventBet(operation: WalletOperation): OperationOutcome {
        val betId = requireNotNull(operation.eventBetId)
        val bet =
            eventBetRepository.findBetByIdForUpdate(betId)
                ?: return OperationOutcome.failed("RESOURCE_NOT_FOUND")

        return when (operation.kind) {
            WalletOperationKind.STAKE -> {
                val round =
                    eventBetRepository.findRoundByIdForUpdate(bet.roundId)
                        ?: return OperationOutcome.failed("RESOURCE_NOT_FOUND")

                if (round.status == EventBetRoundStatus.SETTLED) {
                    bet.rejectLateStake(operation.amount)
                    eventBetRepository.saveBet(bet)

                    requestWalletOperationUseCase.requestCredit(
                        RequestWalletCreditCommand(
                            accountId = bet.accountId,
                            amount = operation.amount,
                            kind = WalletOperationKind.REFUND,
                            eventBetId = bet.id,
                            eventBetResolutionId = null,
                            raceParticipantId = null,
                            eligibleAt = clock.instant(),
                        ),
                    )

                    OperationOutcome.rejected("BETTING_CLOSED")
                } else {
                    bet.confirmStake(operation.amount)
                    eventBetRepository.saveBet(bet)

                    OperationOutcome.confirmed()
                }
            }

            WalletOperationKind.PAYOUT -> {
                bet.completePayout()
                eventBetRepository.saveBet(bet)

                OperationOutcome.confirmed()
            }

            WalletOperationKind.REFUND -> {
                if (operation.eventBetResolutionId != null) {
                    bet.completeRefund()
                    eventBetRepository.saveBet(bet)
                }

                OperationOutcome.confirmed()
            }
        }
    }

    private fun processItemRace(operation: WalletOperation): OperationOutcome {
        val participantId = requireNotNull(operation.raceParticipantId)
        val participant =
            itemRaceRepository.findParticipantByIdForUpdate(participantId)
                ?: return OperationOutcome.failed("RESOURCE_NOT_FOUND")

        return when (operation.kind) {
            WalletOperationKind.STAKE -> {
                val race =
                    itemRaceRepository.findByIdForUpdate(participant.raceId)
                        ?: return OperationOutcome.failed("RESOURCE_NOT_FOUND")
                val now = clock.instant()

                if (race.status == ItemRaceStatus.REGISTRATION && now.isBefore(race.startsAt)) {
                    participant.confirmEntry(operation.amount)
                    itemRaceRepository.saveParticipant(participant)

                    OperationOutcome.confirmed()
                } else {
                    participant.rejectLateEntry(operation.amount)
                    itemRaceRepository.saveParticipant(participant)

                    requestWalletOperationUseCase.requestCredit(
                        RequestWalletCreditCommand(
                            accountId = participant.accountId,
                            amount = operation.amount,
                            kind = WalletOperationKind.REFUND,
                            eventBetId = null,
                            eventBetResolutionId = null,
                            raceParticipantId = participant.id,
                            eligibleAt = now,
                        ),
                    )

                    OperationOutcome.rejected("REGISTRATION_CLOSED")
                }
            }

            WalletOperationKind.PAYOUT -> {
                participant.completePayout()
                itemRaceRepository.saveParticipant(participant)

                OperationOutcome.confirmed()
            }

            WalletOperationKind.REFUND -> {
                participant.completeRefund()
                itemRaceRepository.saveParticipant(participant)

                OperationOutcome.confirmed()
            }
        }
    }

    private data class OperationOutcome(
        val status: BetOperationStatus,
        val failureReason: String?,
    ) {
        companion object {
            fun confirmed(): OperationOutcome = OperationOutcome(BetOperationStatus.CONFIRMED, null)

            fun rejected(reason: String): OperationOutcome = OperationOutcome(BetOperationStatus.REJECTED, reason)

            fun failed(reason: String): OperationOutcome = OperationOutcome(BetOperationStatus.FAILED, reason)
        }
    }
}
