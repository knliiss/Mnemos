package com.mnemos.personalization.application.revenue

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ProcessCreatorRevenueWalletResultServiceTest {
    private val now = Instant.parse("2026-08-15T19:30:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `matching wallet result confirms creator revenue`() {
        val operation = operation()
        val repository = FakeRepository(operation)
        val service = service(repository)
        val transactionId = UUID.fromString("019c1190-0000-7000-8000-000000000001")

        service.complete(
            result = walletResult(operation),
            transactionId = transactionId,
        )

        assertEquals(CreatorRevenueOperationStatus.CONFIRMED, operation.status)
        assertEquals(transactionId, operation.walletTransactionId)
        assertEquals(1, repository.saveCalls)
    }

    @Test
    fun `creator revenue result with actor metadata is rejected`() {
        val operation = operation()
        val repository = FakeRepository(operation)
        val service = service(repository)
        val result =
            walletResult(operation).copy(
                actorUserId = UserId.generate().id,
            )

        assertThrows(IllegalStateException::class.java) {
            service.complete(
                result = result,
                transactionId = UUID.fromString("019c1190-0000-7000-8000-000000000002"),
            )
        }

        assertEquals(CreatorRevenueOperationStatus.PENDING, operation.status)
        assertNull(operation.walletTransactionId)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun `creator revenue result with different amount is rejected`() {
        val operation = operation()
        val repository = FakeRepository(operation)
        val service = service(repository)
        val result = walletResult(operation).copy(amount = operation.amountPrime + 1)

        assertThrows(IllegalStateException::class.java) {
            service.fail(
                result = result,
                reason = "TEMPORARY_FAILURE",
            )
        }

        assertEquals(CreatorRevenueOperationStatus.PENDING, operation.status)
        assertNull(operation.failureReason)
        assertEquals(0, repository.saveCalls)
    }

    private fun service(repository: CreatorRevenueOperationRepository): ProcessCreatorRevenueWalletResultService =
        ProcessCreatorRevenueWalletResultService(
            repository = repository,
            properties = CreatorRevenueProperties(),
            clock = clock,
        )

    private fun operation(): CreatorRevenueOperation =
        CreatorRevenueOperation.create(
            purchaseOperationId = PurchaseOperationId.generate(),
            creatorUserId = UserId.generate(),
            amountPrime = 25,
            now = now.minusSeconds(30),
        )

    private fun walletResult(operation: CreatorRevenueOperation): WalletResultContext =
        WalletResultContext(
            requestId = operation.requestId,
            actorUserId = null,
            ownerType = WalletOwnerType.USER,
            ownerId = operation.creatorUserId.id,
            currency = Currency.PRIME,
            transactionType = TransactionType.CREDIT,
            source = TransactionSource.CREATOR_REVENUE,
            sourceReferenceId = operation.id.id,
            amount = operation.amountPrime,
        )

    private class FakeRepository(
        private val operation: CreatorRevenueOperation,
    ) : CreatorRevenueOperationRepository {
        var saveCalls = 0

        override fun save(operation: CreatorRevenueOperation): CreatorRevenueOperation {
            saveCalls += 1

            return operation
        }

        override fun findByRequestIdForUpdate(requestId: UUID): CreatorRevenueOperation? = operation.takeIf { it.requestId == requestId }

        override fun existsByPurchaseOperationId(purchaseOperationId: PurchaseOperationId): Boolean = false
    }
}
