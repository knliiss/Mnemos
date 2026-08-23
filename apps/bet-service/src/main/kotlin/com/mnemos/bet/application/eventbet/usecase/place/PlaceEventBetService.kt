package com.mnemos.bet.application.eventbet.usecase.place

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.bet.application.wallet.support.WalletOperationIdempotencySupport
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletStakeCommand
import com.mnemos.bet.configuration.EventBetProperties
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.wallet.model.WalletOperationRequestType
import com.mnemos.common.core.model.permission.RolePermission.PLACE_BETS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class PlaceEventBetService(
    private val repository: EventBetRepository,
    private val accountProvider: AccountProvider,
    private val currentActorProvider: CurrentActorProvider,
    private val idempotencySupport: WalletOperationIdempotencySupport,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val properties: EventBetProperties,
    private val clock: Clock,
) : PlaceEventBetUseCase {
    @Transactional
    override fun execute(command: PlaceEventBetCommand): PlaceEventBetResult {
        require(command.amount in properties.minimumBet..properties.maximumBet)

        val account = accountProvider.requireActiveAccount(PLACE_BETS)
        val actorUserId = currentActorProvider.requireUserId()
        val replay =
            command.idempotencyKey?.let { idempotencyKey ->
                idempotencySupport.findStakeReplay(
                    idempotencyKey = idempotencyKey,
                    requestType = WalletOperationRequestType.EVENT_BET_PLACE,
                    accountId = account.id,
                    initiatedByUserId = actorUserId,
                    amount = command.amount,
                )
            }

        if (replay != null) {
            val bet =
                repository.findBetByRoundAndAccountForUpdate(command.roundId, account.id)
                    ?: throw BetConflictException("Idempotency key points to a missing event bet")

            if (replay.eventBetId != bet.id || replay.raceParticipantId != null || bet.side != command.side) {
                throw BetConflictException("Idempotency key is already used by another request")
            }

            return PlaceEventBetResult(
                betId = bet.id,
                operationId = replay.id,
            )
        }

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
                    initiatedByUserId = actorUserId,
                    amount = command.amount,
                    eventBetId = bet.id,
                    raceParticipantId = null,
                    eligibleAt = round.closesAt,
                    requestType = command.idempotencyKey?.let { WalletOperationRequestType.EVENT_BET_PLACE },
                    idempotencyKey = command.idempotencyKey,
                ),
            )

        return PlaceEventBetResult(
            betId = bet.id,
            operationId = operation.id,
        )
    }
}
