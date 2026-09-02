package com.mnemos.bet.application.itemrace.usecase.increase

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.bet.application.wallet.support.WalletOperationIdempotencySupport
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletStakeCommand
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperationRequestType
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission.PLACE_BETS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class IncreaseItemRaceBetService(
    private val repository: ItemRaceRepository,
    private val accountProvider: AccountProvider,
    private val currentActorProvider: CurrentActorProvider,
    private val idempotencySupport: WalletOperationIdempotencySupport,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val clock: Clock,
) : IncreaseItemRaceBetUseCase {
    @Transactional
    override fun execute(command: IncreaseItemRaceBetCommand): IncreaseItemRaceBetResult =
        executeFor(
            command = command,
            actorUserId = currentActorProvider.requireUserId(),
        )

    @Transactional
    fun executeFor(
        command: IncreaseItemRaceBetCommand,
        actorUserId: UserId,
    ): IncreaseItemRaceBetResult {
        require(command.amount > 0)

        val account = accountProvider.requireActiveAccount(actorUserId, PLACE_BETS)
        val replay =
            command.idempotencyKey?.let { idempotencyKey ->
                idempotencySupport.findStakeReplay(
                    idempotencyKey = idempotencyKey,
                    requestType = WalletOperationRequestType.ITEM_RACE_INCREASE,
                    accountId = account.id,
                    initiatedByUserId = actorUserId,
                    amount = command.amount,
                )
            }

        if (replay != null) {
            val participantId =
                replay.raceParticipantId
                    ?: throw BetConflictException("Idempotency key is already used by another request")
            val participant =
                repository.findParticipantByIdForUpdate(participantId)
                    ?: throw BetConflictException("Idempotency key points to a missing item race participant")

            if (replay.eventBetId != null || participant.raceId != command.raceId || participant.accountId != account.id) {
                throw BetConflictException("Idempotency key is already used by another request")
            }

            return IncreaseItemRaceBetResult(
                participantId = participant.id,
                operationId = replay.id,
            )
        }

        val race =
            repository.findByIdForUpdate(command.raceId)
                ?: throw BetNotFoundException("Item race ${command.raceId} was not found")

        race.requireRegistrationOpen(clock.instant())

        val participant =
            repository.findParticipantByRaceAndAccountForUpdate(race.id, account.id)
                ?: throw BetNotFoundException("Item race participant for race ${race.id} was not found")

        participant.increasePending(command.amount)
        repository.saveParticipant(participant)

        val operation =
            requestWalletOperationUseCase.requestStake(
                RequestWalletStakeCommand(
                    accountId = account.id,
                    initiatedByUserId = actorUserId,
                    amount = command.amount,
                    eventBetId = null,
                    raceParticipantId = participant.id,
                    eligibleAt = race.registrationClosesAt,
                    requestType = command.idempotencyKey?.let { WalletOperationRequestType.ITEM_RACE_INCREASE },
                    idempotencyKey = command.idempotencyKey,
                ),
            )

        return IncreaseItemRaceBetResult(
            participantId = participant.id,
            operationId = operation.id,
        )
    }
}
