package com.mnemos.bet.application.eventbet.usecase.increase

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletStakeCommand
import com.mnemos.bet.configuration.EventBetProperties
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.common.core.model.permission.RolePermission.PLACE_BETS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class IncreaseEventBetService(
    private val repository: EventBetRepository,
    private val accountProvider: AccountProvider,
    private val currentActorProvider: CurrentActorProvider,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val properties: EventBetProperties,
    private val clock: Clock,
) : IncreaseEventBetUseCase {
    @Transactional
    override fun execute(command: IncreaseEventBetCommand): IncreaseEventBetResult {
        require(command.amount > 0)

        val account = accountProvider.requireActiveAccount(PLACE_BETS)
        val round =
            repository.findRoundByIdForUpdate(command.roundId)
                ?: throw BetNotFoundException("Event bet round ${command.roundId} was not found")

        round.requireBettingOpen(clock.instant())

        val bet =
            repository.findBetByRoundAndAccountForUpdate(round.id, account.id)
                ?: throw BetNotFoundException("Event bet for round ${round.id} was not found")

        bet.increasePending(
            amount = command.amount,
            maximumTotal = properties.maximumBet,
        )
        repository.saveBet(bet)

        val operation =
            requestWalletOperationUseCase.requestStake(
                RequestWalletStakeCommand(
                    accountId = account.id,
                    initiatedByUserId = currentActorProvider.requireUserId(),
                    amount = command.amount,
                    eventBetId = bet.id,
                    raceParticipantId = null,
                    eligibleAt = round.closesAt,
                ),
            )

        return IncreaseEventBetResult(
            betId = bet.id,
            operationId = operation.id,
        )
    }
}
