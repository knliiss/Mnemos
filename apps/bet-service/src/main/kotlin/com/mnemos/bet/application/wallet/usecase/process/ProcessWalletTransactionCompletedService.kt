package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.application.wallet.usecase.request.RequestWalletCreditCommand
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
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

        when {
            operation.eventBetId != null -> processEventBet(operation)
            operation.raceParticipantId != null -> processItemRace(operation)
        }
    }

    private fun processEventBet(operation: WalletOperation) {
        val betId = requireNotNull(operation.eventBetId)
        val bet = eventBetRepository.findBetByIdForUpdate(betId) ?: return

        when (operation.kind) {
            WalletOperationKind.STAKE -> {
                val round = eventBetRepository.findRoundByIdForUpdate(bet.roundId) ?: return

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
                } else {
                    bet.confirmStake(operation.amount)
                    eventBetRepository.saveBet(bet)
                }
            }

            WalletOperationKind.PAYOUT -> {
                bet.completePayout()
                eventBetRepository.saveBet(bet)
            }

            WalletOperationKind.REFUND -> {
                if (operation.eventBetResolutionId != null) {
                    bet.completeRefund()
                    eventBetRepository.saveBet(bet)
                }
            }
        }
    }

    private fun processItemRace(operation: WalletOperation) {
        val participantId = requireNotNull(operation.raceParticipantId)
        val participant = itemRaceRepository.findParticipantByIdForUpdate(participantId) ?: return

        when (operation.kind) {
            WalletOperationKind.STAKE -> {
                val race = itemRaceRepository.findByIdForUpdate(participant.raceId) ?: return
                val now = clock.instant()

                if (race.status == ItemRaceStatus.REGISTRATION && now.isBefore(race.startsAt)) {
                    participant.confirmEntry(operation.amount)
                    itemRaceRepository.saveParticipant(participant)
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
                }
            }

            WalletOperationKind.PAYOUT -> {
                participant.completePayout()
                itemRaceRepository.saveParticipant(participant)
            }

            WalletOperationKind.REFUND -> {
                participant.completeRefund()
                itemRaceRepository.saveParticipant(participant)
            }
        }
    }
}
