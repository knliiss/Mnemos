package com.mnemos.bet.application.eventbet.usecase.place

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletStakeCommand
import com.mnemos.bet.configuration.EventBetProperties
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class PlaceEventBetService(
    private val repository: EventBetRepository,
    private val accountProvider: AccountProvider,
    private val currentActorProvider: CurrentActorProvider,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val properties: EventBetProperties,
    private val clock: Clock,
) : PlaceEventBetUseCase {
    @Transactional
    override fun execute(command: PlaceEventBetCommand): PlaceEventBetResult {
        require(command.amount in properties.minimumBet..properties.maximumBet)

        val account = accountProvider.requireActiveAccount()
        val round =
            repository.findRoundByIdForUpdate(command.roundId)
                ?: throw BetNotFoundException("Event bet round ${command.roundId} was not found")

        round.requireBettingOpen(clock.instant())

        if (repository.findBetByRoundAndAccountForUpdate(round.id, account.id) != null) {
            throw BetConflictException("Account already has a bet in this round")
        }

        val bet =
            EventBet.create(
                roundId = round.id,
                accountId = account.id,
                side = command.side,
                amount = command.amount,
            )

        repository.createBet(bet)

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

        return PlaceEventBetResult(
            betId = bet.id,
            operationId = operation.id,
        )
    }
}
