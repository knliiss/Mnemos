package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.application.wallet.event.BetOperationEventPublisher
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletCreditCommand
import com.mnemos.bet.application.wallet.usecase.request.RequestWalletOperationUseCase
import com.mnemos.bet.contracts.shared.BetOperationStatus
import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.itemrace.model.DomainItemType
import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantStatus
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.bet.domain.wallet.model.WalletOperation
import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletTransactionId
import com.mnemos.bet.domain.wallet.repository.WalletOperationRepository
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ProcessWalletTransactionCompletedServiceTest {
    private val walletOperationRepository = mock<WalletOperationRepository>()
    private val eventBetRepository = mock<EventBetRepository>()
    private val itemRaceRepository = mock<ItemRaceRepository>()
    private val requestWalletOperationUseCase = mock<RequestWalletOperationUseCase>()
    private val operationEventPublisher = mock<BetOperationEventPublisher>()

    @Test
    fun `late event bet debit is refunded without becoming confirmed stake`() {
        val scheduledAt = Instant.parse("2026-08-14T12:00:00Z")
        val now = scheduledAt.plusSeconds(180)
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val round =
            EventBetRound.create(
                eventType = DomainBetGlobalEventType.MOON,
                scheduledAt = scheduledAt,
                closesAt = scheduledAt.minusSeconds(300),
                resolvesAt = scheduledAt.plusSeconds(120),
                yesOdds = BigDecimal("1.9000"),
                noOdds = BigDecimal("1.9000"),
            )
        round.synchronize(now)
        round.resolveAutomatically(EventBetOutcome.YES)

        val bet =
            EventBet.create(
                roundId = round.id,
                accountId = AccountId.generate(),
                side = DomainBetSide.YES,
                amount = 500,
            )
        bet.settleWithoutPayout()

        val operation =
            WalletOperation.create(
                accountId = bet.accountId,
                amount = 500,
                kind = WalletOperationKind.STAKE,
                eventBetId = bet.id,
                eventBetResolutionId = null,
                raceParticipantId = null,
                createdAt = round.closesAt.minusSeconds(1),
                eligibleAt = round.closesAt,
            )
        val transactionId = WalletTransactionId(UUID.fromString("0198f6ce-e934-7000-8000-000000000010"))

        whenever(walletOperationRepository.findByRequestIdForUpdate(operation.requestId)).thenReturn(operation)
        whenever(eventBetRepository.findBetByIdForUpdate(bet.id)).thenReturn(bet)
        whenever(eventBetRepository.findRoundByIdForUpdate(round.id)).thenReturn(round)

        val service =
            ProcessWalletTransactionCompletedService(
                walletOperationRepository = walletOperationRepository,
                eventBetRepository = eventBetRepository,
                itemRaceRepository = itemRaceRepository,
                requestWalletOperationUseCase = requestWalletOperationUseCase,
                operationEventPublisher = operationEventPublisher,
                clock = clock,
            )

        service.execute(
            ProcessWalletTransactionCompletedCommand(
                requestId = operation.requestId,
                transactionId = transactionId,
            ),
        )

        assertEquals(0, bet.confirmedAmount)
        assertEquals(0, bet.pendingAmount)
        verify(requestWalletOperationUseCase).requestCredit(
            eq(
                RequestWalletCreditCommand(
                    accountId = bet.accountId,
                    amount = 500,
                    kind = WalletOperationKind.REFUND,
                    eventBetId = bet.id,
                    eventBetResolutionId = null,
                    raceParticipantId = null,
                    eligibleAt = now,
                ),
            ),
        )
        verify(operationEventPublisher).publish(
            operation = operation,
            status = BetOperationStatus.REJECTED,
            failureReason = "BETTING_CLOSED",
        )
    }

    @Test
    fun `late item race debit is refunded without entering the bank`() {
        val createdAt = Instant.parse("2026-08-14T12:00:00Z")
        val registrationClosesAt = createdAt.plusSeconds(600)
        val startsAt = registrationClosesAt.plusSeconds(30)
        val now = startsAt.plusSeconds(1)
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val race =
            ItemRace.create(
                itemName = "TestSword",
                itemType = DomainItemType.SWORD,
                registrationClosesAt = registrationClosesAt,
                startsAt = startsAt,
                expiresAt = startsAt.plusSeconds(3600),
                entryAmount = 1_000,
            )
        race.cancel()

        val participant =
            ItemRaceParticipant.create(
                raceId = race.id,
                accountId = AccountId.generate(),
                accountNickname = "Player",
                amount = 1_000,
            )
        val operation =
            WalletOperation.create(
                accountId = participant.accountId,
                amount = 1_000,
                kind = WalletOperationKind.STAKE,
                eventBetId = null,
                eventBetResolutionId = null,
                raceParticipantId = participant.id,
                createdAt = registrationClosesAt.minusSeconds(1),
                eligibleAt = registrationClosesAt,
            )
        val transactionId = WalletTransactionId(UUID.fromString("0198f6ce-e934-7000-8000-000000000011"))

        whenever(walletOperationRepository.findByRequestIdForUpdate(operation.requestId)).thenReturn(operation)
        whenever(itemRaceRepository.findParticipantByIdForUpdate(participant.id)).thenReturn(participant)
        whenever(itemRaceRepository.findByIdForUpdate(race.id)).thenReturn(race)

        val service =
            ProcessWalletTransactionCompletedService(
                walletOperationRepository = walletOperationRepository,
                eventBetRepository = eventBetRepository,
                itemRaceRepository = itemRaceRepository,
                requestWalletOperationUseCase = requestWalletOperationUseCase,
                operationEventPublisher = operationEventPublisher,
                clock = clock,
            )

        service.execute(
            ProcessWalletTransactionCompletedCommand(
                requestId = operation.requestId,
                transactionId = transactionId,
            ),
        )

        assertEquals(0, participant.confirmedAmount)
        assertEquals(0, participant.pendingAmount)
        assertEquals(ItemRaceParticipantStatus.REFUND_PENDING, participant.status)
        verify(requestWalletOperationUseCase).requestCredit(
            eq(
                RequestWalletCreditCommand(
                    accountId = participant.accountId,
                    amount = 1_000,
                    kind = WalletOperationKind.REFUND,
                    eventBetId = null,
                    eventBetResolutionId = null,
                    raceParticipantId = participant.id,
                    eligibleAt = now,
                ),
            ),
        )
        verify(operationEventPublisher).publish(
            operation = operation,
            status = BetOperationStatus.REJECTED,
            failureReason = "REGISTRATION_CLOSED",
        )
    }
}
