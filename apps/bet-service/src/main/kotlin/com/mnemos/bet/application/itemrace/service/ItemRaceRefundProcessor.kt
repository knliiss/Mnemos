package com.mnemos.bet.application.itemrace.service

import com.mnemos.bet.application.wallet.usecase.request.RequestWalletCreditCommand
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import org.springframework.stereotype.Service

@Service
class ItemRaceRefundProcessor(
    private val repository: ItemRaceRepository,
    private val walletOperationRepository: WalletOperationRepository,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
) {
    fun refund(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
    ) {
        participants.forEach { participant ->
            if (participant.confirmedAmount == 0L) {
                return@forEach
            }

            if (
                walletOperationRepository.existsForRaceParticipant(
                    participantId = participant.id,
                    kind = WalletOperationKind.REFUND,
                )
            ) {
                return@forEach
            }

            participant.prepareRefund()
            repository.saveParticipant(participant)

            requestWalletOperationUseCase.requestCredit(
                RequestWalletCreditCommand(
                    accountId = participant.accountId,
                    amount = participant.confirmedAmount,
                    kind = WalletOperationKind.REFUND,
                    eventBetId = null,
                    eventBetResolutionId = null,
                    raceParticipantId = participant.id,
                    eligibleAt = race.startsAt,
                ),
            )
        }
    }
}
