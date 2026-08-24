package com.mnemos.wallet.application

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionRequest
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionResult
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionService
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.balance.repository.BalanceRepository
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.Transaction
import com.mnemos.wallet.domain.transaction.model.TransactionRequest
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId
import com.mnemos.wallet.domain.transaction.repository.TransactionRepository
import com.mnemos.wallet.domain.transaction.repository.TransactionRequestRepository
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateTransactionServiceTest {
    private val balances = InMemoryBalanceRepository()
    private val transactions = InMemoryTransactionRepository()
    private val requests = InMemoryTransactionRequestRepository()
    private val eventPublisher = RecordingIntegrationEventPublisher()

    private val service =
        CreateTransactionService(
            balanceRepository = balances,
            transactionRepository = transactions,
            transactionRequestRepository = requests,
            integrationEventPublisher = eventPublisher,
        )

    @Test
    fun `account coin credit lazily creates only account coin balance`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val request = request(owner = owner, currency = Currency.COIN, type = TransactionType.CREDIT, amount = 100)

        val result =
            service.create(
                request = request,
                metadata = EventMetadata(correlationId = "test"),
            )

        assertInstanceOf(CreateTransactionResult.Completed::class.java, result)
        assertEquals(100, balances.get(owner, Currency.COIN).amount.value)
        assertNull(balances.findForUpdate(owner, Currency.PRIME))
        assertEquals(1, transactions.values.size)
        assertEquals(1, eventPublisher.events.size)
        assertInstanceOf(
            WalletTransactionCompletedEvent::class.java,
            eventPublisher.events
                .single()
                .event.payload,
        )
        assertEquals(
            "test",
            eventPublisher.events
                .single()
                .metadata.correlationId,
        )
    }

    @Test
    fun `user prime credit creates user prime balance`() {
        val owner = WalletOwner.User(UserId.generate())
        val request = request(owner = owner, currency = Currency.PRIME, type = TransactionType.CREDIT, amount = 25)

        service.create(
            request = request,
            metadata = EventMetadata(),
        )

        assertEquals(25, balances.get(owner, Currency.PRIME).amount.value)
        assertEquals(owner, transactions.values.single().owner)
    }

    @Test
    fun `owner currency mismatch is deterministic failure`() {
        val owner = WalletOwner.User(UserId.generate())
        val request = request(owner = owner, currency = Currency.COIN, type = TransactionType.CREDIT, amount = 25)

        val first = service.create(request, EventMetadata())
        val repeated = service.create(request, EventMetadata())

        assertEquals(
            WalletTransactionFailureReason.OWNER_CURRENCY_MISMATCH,
            (first as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            WalletTransactionFailureReason.OWNER_CURRENCY_MISMATCH,
            (repeated as CreateTransactionResult.Failed).reason,
        )
        assertEquals(0, transactions.values.size)
        assertEquals(1, eventPublisher.events.size)
        assertInstanceOf(
            WalletTransactionFailedEvent::class.java,
            eventPublisher.events
                .single()
                .event.payload,
        )
    }

    @Test
    fun `successful duplicate does not apply balance twice`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val request = request(owner = owner, currency = Currency.COIN, type = TransactionType.CREDIT, amount = 100)

        service.create(request, EventMetadata())
        service.create(request, EventMetadata())

        assertEquals(100, balances.get(owner, Currency.COIN).amount.value)
        assertEquals(1, transactions.values.size)
        assertEquals(1, eventPublisher.events.size)
    }

    @Test
    fun `same request id with another payload preserves completed terminal event`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val original = request(owner = owner, currency = Currency.COIN, type = TransactionType.CREDIT, amount = 100)
        val conflicting = original.copy(amount = 200)

        service.create(original, EventMetadata())
        val result = service.create(conflicting, EventMetadata())

        assertEquals(
            WalletTransactionFailureReason.IDEMPOTENCY_CONFLICT,
            (result as CreateTransactionResult.Failed).reason,
        )
        assertEquals(100, balances.get(owner, Currency.COIN).amount.value)
        assertEquals(1, transactions.values.size)
        assertEquals(1, eventPublisher.events.size)
        assertInstanceOf(
            WalletTransactionCompletedEvent::class.java,
            eventPublisher.events
                .single()
                .event.payload,
        )
    }

    @Test
    fun `same request id with another payload preserves failed terminal event`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val original = request(owner = owner, currency = Currency.COIN, type = TransactionType.DEBIT, amount = 50)
        val conflicting = original.copy(amount = 75)

        val first = service.create(original, EventMetadata())
        val conflictingResult = service.create(conflicting, EventMetadata())

        assertEquals(
            WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
            (first as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            WalletTransactionFailureReason.IDEMPOTENCY_CONFLICT,
            (conflictingResult as CreateTransactionResult.Failed).reason,
        )
        assertEquals(0, transactions.values.size)
        assertEquals(1, eventPublisher.events.size)

        val publishedFailure =
            assertInstanceOf(
                WalletTransactionFailedEvent::class.java,
                eventPublisher.events
                    .single()
                    .event.payload,
            )

        assertEquals(
            WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
            publishedFailure.reason,
        )
    }

    @Test
    fun `failed debit stays failed after later credit`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val debit = request(owner = owner, currency = Currency.COIN, type = TransactionType.DEBIT, amount = 50)

        val first = service.create(debit, EventMetadata())
        service.create(
            request(owner = owner, currency = Currency.COIN, type = TransactionType.CREDIT, amount = 100),
            EventMetadata(),
        )
        val repeated = service.create(debit, EventMetadata())

        assertEquals(
            WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
            (first as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
            (repeated as CreateTransactionResult.Failed).reason,
        )
        assertEquals(100, balances.get(owner, Currency.COIN).amount.value)
        assertEquals(1, transactions.values.size)
    }

    @Test
    fun `non positive amount is stored as deterministic failure`() {
        val owner = WalletOwner.Account(AccountId.generate())
        val request = request(owner = owner, currency = Currency.COIN, type = TransactionType.CREDIT, amount = 0)

        val first = service.create(request, EventMetadata())
        val repeated = service.create(request, EventMetadata())

        assertEquals(
            WalletTransactionFailureReason.INVALID_AMOUNT,
            (first as CreateTransactionResult.Failed).reason,
        )
        assertEquals(
            WalletTransactionFailureReason.INVALID_AMOUNT,
            (repeated as CreateTransactionResult.Failed).reason,
        )
        assertEquals(0, transactions.values.size)
    }

    private fun request(
        owner: WalletOwner,
        currency: Currency,
        type: TransactionType,
        amount: Long,
    ): CreateTransactionRequest =
        CreateTransactionRequest(
            requestId = RequestId.generate(),
            owner = owner,
            currency = currency,
            type = type,
            source = TransactionSource.ADMIN_ADJUSTMENT,
            sourceReferenceId = TransactionSourceReferenceId.generate(),
            amount = amount,
        )

    private class InMemoryBalanceRepository : BalanceRepository {
        private val values = linkedMapOf<Triple<WalletOwnerType, UUID, Currency>, Balance>()

        override fun findForUpdate(
            owner: WalletOwner,
            currency: Currency,
        ): Balance? = values[key(owner, currency)]

        override fun createIfAbsent(balance: Balance): Boolean = values.putIfAbsent(key(balance.owner, balance.currency), balance) == null

        override fun update(balance: Balance) {
            values[key(balance.owner, balance.currency)] = balance
        }

        fun get(
            owner: WalletOwner,
            currency: Currency,
        ): Balance = requireNotNull(values[key(owner, currency)])

        private fun key(
            owner: WalletOwner,
            currency: Currency,
        ) = Triple(owner.type, owner.id, currency)
    }

    private class InMemoryTransactionRepository : TransactionRepository {
        val values = mutableListOf<Transaction>()

        override fun create(transaction: Transaction) {
            values += transaction
        }
    }

    private class InMemoryTransactionRequestRepository : TransactionRequestRepository {
        private val values = linkedMapOf<RequestId, TransactionRequest>()

        override fun createIfAbsent(request: TransactionRequest): Boolean = values.putIfAbsent(request.requestId, request) == null

        override fun findByRequestIdForUpdate(requestId: RequestId): TransactionRequest? = values[requestId]

        override fun update(request: TransactionRequest) {
            values[request.requestId] = request
        }
    }

    private class RecordingIntegrationEventPublisher : IntegrationEventPublisher {
        val events = mutableListOf<PublishedEvent>()

        override fun <T : Any> publish(
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            events += PublishedEvent(event, metadata)
            return RequestId.generate().id
        }
    }

    private data class PublishedEvent(
        val event: IntegrationEvent<*>,
        val metadata: EventMetadata,
    )
}
