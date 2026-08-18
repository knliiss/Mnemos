package com.mnemos.bet.application.eventbet.usecase.increase

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.bet.application.wallet.support.WalletOperationIdempotencySupport
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletStakeCommand
import com.mnemos.bet.configuration.EventBetProperties
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.wallet.model.WalletOperationRequestType
import com.mnemos.common.core.model.permission.RolePermission.PLACE_BETS
import com.mnemos.common.core.model.permission.RolePermission.SPEND_WALLET_FUNDS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class IncreaseEventBetService(
    private val repository: EventBetRepository,
    private val accountProvider: AccountProvider,
    private val currentActorProvider: CurrentActorProvider,
    private val idempotencySupport: WalletOperationIdempotencySupport,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val properties: EventBetProperties,
    private val clock: Clock,
) : IncreaseEventBetUseCase {
    @Transactional
    override fun execute(command: IncreaseEventBetCommand): IncreaseEventBetResult {
        require(command.amount > 0)

        val account = accountProvider.requireActiveAccount(setOf(PLACE_BETS, SPEND_WALLET_FUNDS))
        val actorUserId = currentActorProvider.requireUserId()
        val replay =
            command.idempotencyKey?.let { idempotencyKey ->
                idempotencySupport.findStakeReplay(
                    idempotencyKey = idempotencyKey,
                    requestType = WalletOperationRequestType.EVENT_BET_INCREASE,
                    accountId = account.id,
                    initiatedByUserId = actorUserId,
                    amount = command.amount,
                )
            }

        if (replay != null) {
            val bet =
                repository.findBetByRoundAndAccountForUpdate(command.roundId, account.id)
                    ?: throw BetConflictException("Idempotency key points to a missing event bet")

            if (replay.eventBetId != bet.id || replay.raceParticipantId != null) {
                throw BetConflictException("Idempotency key is already used by another request")
            }

            return IncreaseEventBetResult(
                betId = bet.id,
                operationId = replay.id,
            )
        }

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
                    initiatedByUserId = actorUserId,
                    amount = command.amount,
                    eventBetId = bet.id,
                    raceParticipantId = null,
                    eligibleAt = round.closesAt,
                    requestType = command.idempotencyKey?.let { WalletOperationRequestType.EVENT_BET_INCREASE },
                    idempotencyKey = command.idempotencyKey,
                ),
            )

        return IncreaseEventBetResult(
            betId = bet.id,
            operationId = operation.id,
        )
    }
}
