package com.mnemos.bet.application.activity.usecase

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.activity.model.MyActiveBetActivity
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.permission.RolePermission.VIEW_BETS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMyActiveBetActivityService(
    private val accountProvider: AccountProvider,
    private val eventBetReader: EventBetReader,
    private val itemRaceReader: ItemRaceReader,
    private val walletOperationRepository: WalletOperationRepository,
) {
    fun execute(): MyActiveBetActivity {
        val account = accountProvider.requireActiveAccount(VIEW_BETS)
        val accountId = account.id
        val eventBets =
            eventBetReader
                .findCurrentRounds()
                .mapNotNull { round ->
                    val bet = eventBetReader.findBet(round.id, accountId) ?: return@mapNotNull null

                    MyActiveBetActivity.ActiveEventBet(
                        round = round,
                        bet = bet,
                    )
                }
        val itemRaces =
            itemRaceReader
                .findCurrent()
                .mapNotNull { race ->
                    val participant = itemRaceReader.findParticipant(race.id, accountId) ?: return@mapNotNull null

                    MyActiveBetActivity.ActiveItemRace(
                        race = race,
                        participant = participant,
                    )
                }
        val pendingOperations = walletOperationRepository.findPendingByAccountId(accountId)

        return MyActiveBetActivity(
            eventBets = eventBets,
            itemRaces = itemRaces,
            pendingOperations = pendingOperations,
        )
    }
}
