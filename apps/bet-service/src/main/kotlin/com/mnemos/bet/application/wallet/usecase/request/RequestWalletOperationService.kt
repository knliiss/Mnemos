package com.mnemos.bet.application.wallet.usecase.request

import com.mnemos.bet.application.wallet.port.out.BetWallet
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetResolutionId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class RequestWalletOperationService(
    private val repository: WalletOperationRepository,
    private val wallet: BetWallet,
    private val clock: Clock,
) : RequestWalletOperationUseCase {
    override fun requestStake(command: RequestWalletStakeCommand): WalletOperation {
        require((command.eventBetId != null) xor (command.raceParticipantId != null))

        return createAndPublish(
            accountId = command.accountId,
            initiatedByUserId = command.initiatedByUserId,
            amount = command.amount,
            kind = WalletOperationKind.STAKE,
            eventBetId = command.eventBetId,
            eventBetResolutionId = null,
            raceParticipantId = command.raceParticipantId,
            eligibleAt = command.eligibleAt,
        )
    }

    override fun requestCredit(command: RequestWalletCreditCommand): WalletOperation {
        require(command.kind == WalletOperationKind.PAYOUT || command.kind == WalletOperationKind.REFUND)
        require((command.eventBetId != null) xor (command.raceParticipantId != null))

        return createAndPublish(
            accountId = command.accountId,
            initiatedByUserId = null,
            amount = command.amount,
            kind = command.kind,
            eventBetId = command.eventBetId,
            eventBetResolutionId = command.eventBetResolutionId,
            raceParticipantId = command.raceParticipantId,
            eligibleAt = command.eligibleAt,
        )
    }

    private fun createAndPublish(
        accountId: AccountId,
        initiatedByUserId: UserId?,
        amount: Long,
        kind: WalletOperationKind,
        eventBetId: EventBetId?,
        eventBetResolutionId: EventBetResolutionId?,
        raceParticipantId: ItemRaceParticipantId?,
        eligibleAt: Instant,
    ): WalletOperation {
        val operation =
            WalletOperation.create(
                accountId = accountId,
                initiatedByUserId = initiatedByUserId,
                amount = amount,
                kind = kind,
                eventBetId = eventBetId,
                eventBetResolutionId = eventBetResolutionId,
                raceParticipantId = raceParticipantId,
                createdAt = clock.instant(),
                eligibleAt = eligibleAt,
            )

        repository.create(operation)
        wallet.request(operation)

        return operation
    }
}
