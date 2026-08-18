package com.mnemos.bet.application.itemrace.usecase.join

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.bet.application.wallet.support.WalletOperationIdempotencySupport
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletStakeCommand
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperationRequestType
import com.mnemos.common.core.model.permission.RolePermission.PLACE_BETS
import com.mnemos.common.core.model.permission.RolePermission.SPEND_WALLET_FUNDS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class JoinItemRaceService(
    private val repository: ItemRaceRepository,
    private val accountProvider: AccountProvider,
    private val currentActorProvider: CurrentActorProvider,
    private val idempotencySupport: WalletOperationIdempotencySupport,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val clock: Clock,
) : JoinItemRaceUseCase {
    @Transactional
    override fun execute(command: JoinItemRaceCommand): JoinItemRaceResult {
        val account = accountProvider.requireActiveAccount(setOf(PLACE_BETS, SPEND_WALLET_FUNDS))
        val actorUserId = currentActorProvider.requireUserId()
        val race =
            repository.findByIdForUpdate(command.raceId)
                ?: throw BetNotFoundException("Item race ${command.raceId} was not found")
        val replay =
            command.idempotencyKey?.let { idempotencyKey ->
                idempotencySupport.findStakeReplay(
                    idempotencyKey = idempotencyKey,
                    requestType = WalletOperationRequestType.ITEM_RACE_JOIN,
                    accountId = account.id,
                    initiatedByUserId = actorUserId,
                    amount = race.entryAmount,
                )
            }

        if (replay != null) {
            val participant =
                repository.findParticipantByRaceAndAccountForUpdate(race.id, account.id)
                    ?: throw BetConflictException("Idempotency key points to a missing item race participant")

            if (replay.raceParticipantId != participant.id || replay.eventBetId != null) {
                throw BetConflictException("Idempotency key is already used by another request")
            }

            return JoinItemRaceResult(
                participantId = participant.id,
                operationId = replay.id,
            )
        }

        race.requireRegistrationOpen(clock.instant())

        if (repository.findParticipantByRaceAndAccountForUpdate(race.id, account.id) != null) {
            throw BetConflictException("Account already participates in this item race")
        }

        val participant =
            ItemRaceParticipant.create(
                raceId = race.id,
                accountId = account.id,
                accountNickname = account.nickname,
                amount = race.entryAmount,
            )

        repository.createParticipant(participant)

        val operation =
            requestWalletOperationUseCase.requestStake(
                RequestWalletStakeCommand(
                    accountId = account.id,
                    initiatedByUserId = actorUserId,
                    amount = race.entryAmount,
                    eventBetId = null,
                    raceParticipantId = participant.id,
                    eligibleAt = race.registrationClosesAt,
                    requestType = command.idempotencyKey?.let { WalletOperationRequestType.ITEM_RACE_JOIN },
                    idempotencyKey = command.idempotencyKey,
                ),
            )

        return JoinItemRaceResult(
            participantId = participant.id,
            operationId = operation.id,
        )
    }
}
