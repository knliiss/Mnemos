package com.mnemos.wallet.application

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.wallet.api.dto.BalanceResponse
import com.mnemos.wallet.api.dto.TransactionSummaryResponse
import com.mnemos.wallet.application.balance.InitializeAccountBalancesService
import com.mnemos.wallet.application.transaction.CreateTransactionRequest
import com.mnemos.wallet.application.transaction.CreateTransactionResult
import com.mnemos.wallet.application.transaction.CreateTransactionService
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.model.balance.Balance
import com.mnemos.wallet.domain.model.request.TransactionRequest
import com.mnemos.wallet.domain.model.request.TransactionRequestStatus
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.Transaction
import com.mnemos.wallet.domain.model.transaction.TransactionSourceReferenceId
import com.mnemos.wallet.domain.repository.BalanceRepository
import com.mnemos.wallet.domain.repository.TransactionRepository
import com.mnemos.wallet.domain.repository.TransactionRequestRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import java.util.UUID

class CreateTransactionServiceTest {
    private val balances = InMemoryBalanceRepository()
    private val transactions = InMemoryTransactionRepository()
    private val requests = InMemoryTransactionRequestRepository()
    private val eventPublisher = RecordingIntegrationEventPublisher()

    private val service =
        CreateTransactionService(
            initializeAccountBalancesService = InitializeAccountBalancesService(balances),
            balanceRepository = balances,
            transactionRepository = transactions,
            transactionRequestRepository = requests,
            integrationEventPublisher = eventPublisher,
        )

    @Test
    fun `credit lazily initializes both balances and creates transaction`() {
        val request = request(type = TransactionType.CREDIT, amount = 100)

        val result =
            service.create(
                request = request,
                metadata = EventMetadata(correlationId = "test"),
            )

        assertInstanceOf(
            CreateTransactionResult.Completed::class.java,
            result,
        )
        assertEquals(
            100,
            balances
                .get(
                    request.accountId,
                    Currency.COIN,
                ).amount.value,
        )
        assertEquals(
            0,
            balances
                .get(
                    request.accountId,
                    Currency.PRIME,
                ).amount.value,
        )
        assertEquals(
            1,
            transactions.values.size,
        )
        assertEquals(
            1,
            eventPublisher.events.size,
        )
        assertInstanceOf(
            WalletTransactionCompletedEvent::class.java,
            eventPublisher.events
                .single()
                .event
                .payload,
        )
        assertEquals(
            "test",
            eventPublisher.events
                .single()
                .metadata
                .correlationId,
        )
    }

    @Test
    fun `successful duplicate does not apply balance twice`() {
        val request = request(type = TransactionType.CREDIT, amount = 100)

        service.create(
            request = request,
            metadata = EventMetadata(),
        )
        service.create(
            request = request,
            metadata = EventMetadata(),
        )

        assertEquals(
            100,
            balances
                .get(
                    request.accountId,
                    Currency.COIN,
                ).amount.value,
        )
        assertEquals(
            1,
            transactions.values.size,
        )
        assertEquals(
            2,
            eventPublisher.events.size,
        )

        eventPublisher.events.forEach {
            assertInstanceOf(
                WalletTransactionCompletedEvent::class.java,
                it.event.payload,
            )
        }
    }

    @Test
    fun `failed debit remains failed after later top up`() {
        val accountId = AccountId.generate()

        val debit =
            request(
                accountId = accountId,
                type = TransactionType.DEBIT,
                amount = 50,
            )

        val firstResult =
            service.create(
                request = debit,
                metadata = EventMetadata(),
            )

        service.create(
            request =
                request(
                    accountId = accountId,
                    type = TransactionType.CREDIT,
                    amount = 100,
                ),
            metadata = EventMetadata(),
        )

        val repeatedResult =
            service.create(
                request = debit,
                metadata = EventMetadata(),
            )

        assertEquals(
            WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
            (firstResult as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
            (repeatedResult as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            100,
            balances
                .get(
                    accountId,
                    Currency.COIN,
                ).amount.value,
        )
        assertEquals(
            1,
            transactions.values.size,
        )

        assertEquals(
            3,
            eventPublisher.events.size,
        )

        assertInstanceOf(
            WalletTransactionFailedEvent::class.java,
            eventPublisher.events[0]
                .event
                .payload,
        )
        assertInstanceOf(
            WalletTransactionCompletedEvent::class.java,
            eventPublisher.events[1]
                .event
                .payload,
        )
        assertInstanceOf(
            WalletTransactionFailedEvent::class.java,
            eventPublisher.events[2]
                .event
                .payload,
        )
    }

    @Test
    fun `same request id with another payload is rejected`() {
        val original =
            request(
                type = TransactionType.CREDIT,
                amount = 100,
            )

        val conflicting =
            original.copy(
                amount = 200,
            )

        service.create(
            request = original,
            metadata = EventMetadata(),
        )

        val result =
            service.create(
                request = conflicting,
                metadata = EventMetadata(),
            )

        assertEquals(
            WalletTransactionFailureReason.IDEMPOTENCY_CONFLICT,
            (result as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            100,
            balances
                .get(
                    original.accountId,
                    Currency.COIN,
                ).amount.value,
        )
        assertEquals(
            1,
            transactions.values.size,
        )
        assertEquals(
            2,
            eventPublisher.events.size,
        )
        assertInstanceOf(
            WalletTransactionFailedEvent::class.java,
            eventPublisher.events
                .last()
                .event
                .payload,
        )
    }

    @Test
    fun `credit that exceeds bigint balance limit is deterministic failure`() {
        val accountId = AccountId.generate()

        service.create(
            request =
                request(
                    accountId = accountId,
                    type = TransactionType.CREDIT,
                    amount = Long.MAX_VALUE,
                ),
            metadata = EventMetadata(),
        )

        val overflow =
            request(
                accountId = accountId,
                type = TransactionType.CREDIT,
                amount = 1,
            )

        val first =
            service.create(
                request = overflow,
                metadata = EventMetadata(),
            )

        val repeated =
            service.create(
                request = overflow,
                metadata = EventMetadata(),
            )

        assertEquals(
            WalletTransactionFailureReason.BALANCE_LIMIT_EXCEEDED,
            (first as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            WalletTransactionFailureReason.BALANCE_LIMIT_EXCEEDED,
            (repeated as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            Long.MAX_VALUE,
            balances
                .get(
                    accountId,
                    Currency.COIN,
                ).amount.value,
        )
        assertEquals(
            1,
            transactions.values.size,
        )

        assertEquals(
            3,
            eventPublisher.events.size,
        )
        assertInstanceOf(
            WalletTransactionFailedEvent::class.java,
            eventPublisher.events
                .last()
                .event
                .payload,
        )
    }

    @Test
    fun `non positive amount is stored as deterministic failure`() {
        val request =
            request(
                type = TransactionType.CREDIT,
                amount = 0,
            )

        val first =
            service.create(
                request = request,
                metadata = EventMetadata(),
            )

        val repeated =
            service.create(
                request = request,
                metadata = EventMetadata(),
            )

        assertEquals(
            WalletTransactionFailureReason.INVALID_AMOUNT,
            (first as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            WalletTransactionFailureReason.INVALID_AMOUNT,
            (repeated as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            0,
            transactions.values.size,
        )
        assertEquals(
            2,
            eventPublisher.events.size,
        )

        eventPublisher.events.forEach {
            assertInstanceOf(
                WalletTransactionFailedEvent::class.java,
                it.event.payload,
            )
        }
    }

    private fun request(
        accountId: AccountId = AccountId.generate(),
        type: TransactionType,
        amount: Long,
    ): CreateTransactionRequest =
        CreateTransactionRequest(
            requestId = RequestId(UUID.randomUUID()),
            accountId = accountId,
            currency = Currency.COIN,
            type = type,
            source = TransactionSource.ADMIN_ADJUSTMENT,
            sourceReferenceId =
                TransactionSourceReferenceId(
                    UUID.randomUUID(),
                ),
            amount = amount,
        )

    private class InMemoryBalanceRepository : BalanceRepository {
        private val values =
            linkedMapOf<Pair<AccountId, Currency>, Balance>()

        override fun findByAccountIdAndCurrencyForUpdate(
            accountId: AccountId,
            currency: Currency,
        ): Balance? =
            values[
                accountId to currency,
            ]

        override fun update(balance: Balance) {
            values[
                balance.accountId to balance.currency,
            ] = balance
        }

        override fun createIfAbsent(balance: Balance): Boolean =
            values.putIfAbsent(
                balance.accountId to balance.currency,
                balance,
            ) == null

        override fun findAllByAccountId(accountId: AccountId): List<BalanceResponse> = emptyList()

        fun get(
            accountId: AccountId,
            currency: Currency,
        ): Balance =
            requireNotNull(
                values[
                    accountId to currency,
                ],
            )
    }

    private class InMemoryTransactionRepository : TransactionRepository {
        val values =
            mutableListOf<Transaction>()

        override fun findByRequestId(requestId: RequestId): Transaction? =
            values.firstOrNull {
                it.requestId == requestId
            }

        override fun create(transaction: Transaction) {
            values += transaction
        }

        override fun findByAccountId(
            accountId: AccountId,
            page: Pageable,
        ): List<TransactionSummaryResponse> = emptyList()
    }

    private class InMemoryTransactionRequestRepository : TransactionRequestRepository {
        private val values =
            linkedMapOf<RequestId, TransactionRequest>()

        override fun createIfAbsent(request: TransactionRequest): Boolean =
            values.putIfAbsent(
                request.requestId,
                request,
            ) == null

        override fun findByRequestIdForUpdate(requestId: RequestId): TransactionRequest? = values[requestId]

        override fun update(request: TransactionRequest) {
            values[request.requestId] =
                request
        }

        override fun findStatusByRequestId(requestId: RequestId): TransactionRequestStatus? = values[requestId]?.status
    }

    private class RecordingIntegrationEventPublisher : IntegrationEventPublisher {
        val events =
            mutableListOf<PublishedEvent>()

        override fun <T : Any> publish(
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            events +=
                PublishedEvent(
                    event = event,
                    metadata = metadata,
                )

            return UUID.randomUUID()
        }
    }

    private data class PublishedEvent(
        val event: IntegrationEvent<*>,
        val metadata: EventMetadata,
    )
}
