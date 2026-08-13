package com.mnemos.bet.application.eventbet.service

import com.mnemos.bet.application.wallet.usecase.request.RequestWalletCreditCommand
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import org.springframework.stereotype.Service
import java.math.RoundingMode

@Service
class EventBetResolutionProcessor(
    private val repository: EventBetRepository,
    private val walletOperationRepository: WalletOperationRepository,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
) {
    fun process(
        round: EventBetRound,
        resolution: EventBetResolution,
        bets: List<EventBet>,
    ) {
        when (resolution.outcome) {
            EventBetOutcome.REFUND -> refund(resolution, bets)
            EventBetOutcome.YES -> settleSide(round, resolution, bets, DomainBetSide.YES)
            EventBetOutcome.NO -> settleSide(round, resolution, bets, DomainBetSide.NO)
        }
    }

    private fun settleSide(
        round: EventBetRound,
        resolution: EventBetResolution,
        bets: List<EventBet>,
        winningSide: DomainBetSide,
    ) {
        bets.forEach { bet ->
            if (bet.side != winningSide || bet.confirmedAmount == 0L) {
                bet.settleWithoutPayout()
                repository.saveBet(bet)
                return@forEach
            }

            if (
                walletOperationRepository.existsForEventBet(
                    eventBetId = bet.id,
                    kind = WalletOperationKind.PAYOUT,
                )
            ) {
                return@forEach
            }

            val payout =
                round
                    .oddsFor(bet.side)
                    .multiply(bet.confirmedAmount.toBigDecimal())
                    .setScale(0, RoundingMode.FLOOR)
                    .longValueExact()

            bet.preparePayout()
            repository.saveBet(bet)

            requestWalletOperationUseCase.requestCredit(
                RequestWalletCreditCommand(
                    accountId = bet.accountId,
                    amount = payout,
                    kind = WalletOperationKind.PAYOUT,
                    eventBetId = bet.id,
                    eventBetResolutionId = resolution.id,
                    raceParticipantId = null,
                    eligibleAt = resolution.resolvedAt,
                ),
            )
        }
    }

    private fun refund(
        resolution: EventBetResolution,
        bets: List<EventBet>,
    ) {
        bets.forEach { bet ->
            if (bet.confirmedAmount == 0L) {
                bet.prepareRefund()
                repository.saveBet(bet)
                return@forEach
            }

            if (
                walletOperationRepository.existsForEventBet(
                    eventBetId = bet.id,
                    kind = WalletOperationKind.REFUND,
                )
            ) {
                return@forEach
            }

            bet.prepareRefund()
            repository.saveBet(bet)

            requestWalletOperationUseCase.requestCredit(
                RequestWalletCreditCommand(
                    accountId = bet.accountId,
                    amount = bet.confirmedAmount,
                    kind = WalletOperationKind.REFUND,
                    eventBetId = bet.id,
                    eventBetResolutionId = resolution.id,
                    raceParticipantId = null,
                    eligibleAt = resolution.resolvedAt,
                ),
            )
        }
    }
}
