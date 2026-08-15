package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.application.wallet.event.BetOperationEventPublisher
import com.mnemos.bet.contracts.shared.BetOperationStatus
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class ProcessWalletTransactionFailedServiceTest {
    private val walletOperationRepository = mock<WalletOperationRepository>()
    private val eventBetRepository = mock<EventBetRepository>()
    private val itemRaceRepository = mock<ItemRaceRepository>()
    private val operationEventPublisher = mock<BetOperationEventPublisher>()
    private val service =
        ProcessWalletTransactionFailedService(
            walletOperationRepository = walletOperationRepository,
            eventBetRepository = eventBetRepository,
            itemRaceRepository = itemRaceRepository,
            operationEventPublisher = operationEventPublisher,
        )

    @Test
    fun `rolls back a pending stake and publishes its final failure`() {
        val bet =
            EventBet.create(
                roundId = EventBetRoundId.generate(),
                accountId = AccountId.generate(),
                side = DomainBetSide.YES,
                amount = 500,
            )
        val operation =
            WalletOperation.create(
                accountId = bet.accountId,
                initiatedByUserId = UserId.generate(),
                amount = 500,
                kind = WalletOperationKind.STAKE,
                eventBetId = bet.id,
                eventBetResolutionId = null,
                raceParticipantId = null,
                createdAt = NOW,
                eligibleAt = NOW,
            )

        whenever(walletOperationRepository.findByRequestIdForUpdate(operation.requestId)).thenReturn(operation)
        whenever(eventBetRepository.findBetByIdForUpdate(bet.id)).thenReturn(bet)

        service.execute(
            ProcessWalletTransactionFailedCommand(
                requestId = operation.requestId,
                reason = WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
            ),
        )

        assertEquals(WalletOperationStatus.FAILED, operation.status)
        assertEquals(0, bet.pendingAmount)
        verify(walletOperationRepository).save(operation)
        verify(eventBetRepository).saveBet(bet)
        verify(operationEventPublisher).publish(
            operation = operation,
            status = BetOperationStatus.FAILED,
            failureReason = WalletTransactionFailureReason.INSUFFICIENT_BALANCE.name,
        )
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-15T10:00:00Z")
    }
}
