package com.mnemos.bet.application.itemrace.usecase.join

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletStakeCommand
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class JoinItemRaceService(
    private val repository: ItemRaceRepository,
    private val accountProvider: AccountProvider,
    private val requestWalletOperationUseCase: RequestWalletOperationUseCase,
    private val clock: Clock,
) : JoinItemRaceUseCase {
    @Transactional
    override fun execute(command: JoinItemRaceCommand): JoinItemRaceResult {
        val account = accountProvider.requireActiveAccount()
        val race =
            repository.findByIdForUpdate(command.raceId)
                ?: throw BetNotFoundException("Item race ${command.raceId} was not found")

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

        requestWalletOperationUseCase.requestStake(
            RequestWalletStakeCommand(
                accountId = account.id,
                amount = race.entryAmount,
                eventBetId = null,
                raceParticipantId = participant.id,
                eligibleAt = race.registrationClosesAt,
            ),
        )

        return JoinItemRaceResult(
            participantId = participant.id,
        )
    }
}
