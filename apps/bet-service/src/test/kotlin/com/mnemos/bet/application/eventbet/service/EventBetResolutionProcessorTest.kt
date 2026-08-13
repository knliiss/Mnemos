package com.mnemos.bet.application.eventbet.service

import com.mnemos.bet.application.wallet.usecase.request.RequestWalletCreditCommand
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.model.EventBetStatus
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant

class EventBetResolutionProcessorTest {
    private val repository = mock<EventBetRepository>()
    private val walletOperationRepository = mock<WalletOperationRepository>()
    private val requestWalletOperationUseCase = mock<RequestWalletOperationUseCase>()

    private val processor =
        EventBetResolutionProcessor(
            repository = repository,
            walletOperationRepository = walletOperationRepository,
            requestWalletOperationUseCase = requestWalletOperationUseCase,
        )

    @Test
    fun `administrative correction pays a previously losing bet`() {
        val round = createRound()
        val bet = createConfirmedBet(round)
        bet.settleWithoutPayout()
        val resolution =
            EventBetResolution.automatic(
                roundId = round.id,
                outcome = EventBetOutcome.YES,
                resolvedAt = round.resolvesAt,
            )

        whenever(
            walletOperationRepository.existsForEventBet(
                eventBetId = bet.id,
                kind = WalletOperationKind.PAYOUT,
            ),
        ).thenReturn(false)

        processor.process(
            round = round,
            resolution = resolution,
            bets = listOf(bet),
        )

        assertEquals(EventBetStatus.SETTLEMENT_PENDING, bet.status)
        verify(requestWalletOperationUseCase).requestCredit(
            eq(
                RequestWalletCreditCommand(
                    accountId = bet.accountId,
                    amount = 190,
                    kind = WalletOperationKind.PAYOUT,
                    eventBetId = bet.id,
                    eventBetResolutionId = resolution.id,
                    raceParticipantId = null,
                    eligibleAt = resolution.resolvedAt,
                ),
            ),
        )
    }

    @Test
    fun `later corrections do not pay the same bet twice`() {
        val round = createRound()
        val bet = createConfirmedBet(round)
        bet.completePayout()
        val resolution =
            EventBetResolution.automatic(
                roundId = round.id,
                outcome = EventBetOutcome.YES,
                resolvedAt = round.resolvesAt,
            )

        whenever(
            walletOperationRepository.existsForEventBet(
                eventBetId = bet.id,
                kind = WalletOperationKind.PAYOUT,
            ),
        ).thenReturn(true)

        processor.process(
            round = round,
            resolution = resolution,
            bets = listOf(bet),
        )

        assertEquals(EventBetStatus.SETTLED, bet.status)
        verify(requestWalletOperationUseCase, never()).requestCredit(any())
    }

    private fun createRound(): EventBetRound {
        val scheduledAt = Instant.parse("2026-08-14T12:00:00Z")

        return EventBetRound.create(
            eventType = DomainBetGlobalEventType.MOON,
            scheduledAt = scheduledAt,
            closesAt = scheduledAt.minusSeconds(300),
            resolvesAt = scheduledAt.plusSeconds(120),
            yesOdds = BigDecimal("1.9000"),
            noOdds = BigDecimal("1.9000"),
        )
    }

    private fun createConfirmedBet(round: EventBetRound): EventBet {
        val bet =
            EventBet.create(
                roundId = round.id,
                accountId = AccountId.generate(),
                side = DomainBetSide.YES,
                amount = 100,
            )

        bet.confirmStake(100)
        return bet
    }
}
