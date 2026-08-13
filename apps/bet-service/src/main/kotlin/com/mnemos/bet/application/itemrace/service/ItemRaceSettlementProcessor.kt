package com.mnemos.bet.application.itemrace.service

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletCreditCommand
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.contracts.BetEventType
import com.mnemos.bet.contracts.event.ItemRaceSettledV1
import com.mnemos.bet.contracts.shared.BetResult
import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.Clock

@Service
class ItemRaceSettlementProcessor(
    private val repository: ItemRaceRepository,
    private val accountProvider: AccountProvider,
    private val walletOperationRepository: WalletOperationRepository,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) {
    fun settleFromDrop(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
        droppedFor: String,
    ) {
        race.beginSettlement()
        repository.save(race)

        val dropAccount = accountProvider.findByNickname(droppedFor)
        val winner =
            dropAccount?.let { account ->
                participants.firstOrNull { participant ->
                    participant.accountId == account.id && participant.confirmedAmount > 0
                }
            }

        if (winner == null) {
            settleWithoutWinner(
                race = race,
                participants = participants,
            )
            return
        }

        settleWithWinner(
            race = race,
            participants = participants,
            winner = winner,
        )
    }

    private fun settleWithoutWinner(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
    ) {
        participants.filter { it.confirmedAmount > 0 }.forEach { participant ->
            participant.lose()
            repository.saveParticipant(participant)
        }

        race.settle(winnerAccountId = null)
        repository.save(race)

        publishSettlement(
            race = race,
            participants = participants,
            winner = null,
            payout = 0,
        )
    }

    private fun settleWithWinner(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
        winner: ItemRaceParticipant,
    ) {
        participants
            .filter { participant ->
                participant.confirmedAmount > 0 && participant.id != winner.id
            }.forEach { participant ->
                participant.lose()
                repository.saveParticipant(participant)
            }

        val bank = participants.sumOf { it.confirmedAmount }
        val payout =
            calculatePayout(
                bank = bank,
                feePercent = race.feePercent,
            )

        if (
            !walletOperationRepository.existsForRaceParticipant(
                participantId = winner.id,
                kind = WalletOperationKind.PAYOUT,
            )
        ) {
            winner.preparePayout()
            repository.saveParticipant(winner)

            requestWalletOperationUseCase.requestCredit(
                RequestWalletCreditCommand(
                    accountId = winner.accountId,
                    amount = payout,
                    kind = WalletOperationKind.PAYOUT,
                    eventBetId = null,
                    eventBetResolutionId = null,
                    raceParticipantId = winner.id,
                    eligibleAt = race.startsAt,
                ),
            )
        }

        race.settle(winnerAccountId = winner.accountId)
        repository.save(race)

        publishSettlement(
            race = race,
            participants = participants,
            winner = winner,
            payout = payout,
        )
    }

    private fun calculatePayout(
        bank: Long,
        feePercent: Int,
    ): Long =
        bank
            .toBigDecimal()
            .multiply((100 - feePercent).toBigDecimal())
            .divide(100.toBigDecimal(), 0, RoundingMode.FLOOR)
            .longValueExact()

    private fun publishSettlement(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
        winner: ItemRaceParticipant?,
        payout: Long,
    ) {
        val settledAt = clock.instant()

        participants.filter { it.confirmedAmount > 0 }.forEach { participant ->
            val won = participant.id == winner?.id

            publisher.publish(
                IntegrationEvent(
                    type = BetEventType.ITEM_RACE_SETTLED,
                    aggregateId = race.id.id,
                    occurredAt = settledAt,
                    payload =
                        ItemRaceSettledV1(
                            raceId = race.id.id,
                            accountId = participant.accountId.id,
                            result = if (won) BetResult.WON else BetResult.LOST,
                            amount = participant.confirmedAmount,
                            payout = if (won) payout else 0,
                            settledAt = settledAt,
                        ),
                ),
            )
        }
    }
}
