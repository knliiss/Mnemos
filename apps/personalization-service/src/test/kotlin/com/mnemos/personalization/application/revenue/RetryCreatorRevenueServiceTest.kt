package com.mnemos.personalization.application.revenue

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.revenue.port.out.CreatorRevenueWallet
import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueRetryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class RetryCreatorRevenueServiceTest {
    private val now = Instant.parse("2026-08-15T16:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `failed creator revenue is retried with a new wallet request id`() {
        val operation = failedOperation()
        val previousRequestId = operation.requestId
        val repository = FakeRepository(operation)
        val wallet = FakeWallet()
        val service =
            RetryCreatorRevenueService(
                operationRepository = repository,
                retryRepository = repository,
                wallet = wallet,
                properties = CreatorRevenueProperties(),
                clock = clock,
            )

        val retried = service.execute()

        assertEquals(1, retried)
        assertEquals(CreatorRevenueOperationStatus.PENDING, operation.status)
        assertNotEquals(previousRequestId, operation.requestId)
        assertEquals(operation.requestId, wallet.requestIds.single())
    }

    private fun failedOperation(): CreatorRevenueOperation =
        CreatorRevenueOperation.create(
            purchaseOperationId = PurchaseOperationId.generate(),
            creatorUserId = UserId.generate(),
            amountPrime = 10,
            now = now.minusSeconds(60),
        ).also { operation ->
            operation.fail(
                reason = "TEMPORARY_FAILURE",
                retryAt = now.minusSeconds(1),
            )
        }

    private class FakeWallet : CreatorRevenueWallet {
        val requestIds = mutableListOf<UUID>()

        override fun requestCredit(operation: CreatorRevenueOperation) {
            requestIds += operation.requestId
        }
    }

    private class FakeRepository(
        private val operation: CreatorRevenueOperation,
    ) : CreatorRevenueOperationRepository,
        CreatorRevenueRetryRepository {
        override fun save(operation: CreatorRevenueOperation): CreatorRevenueOperation = operation

        override fun findByRequestIdForUpdate(requestId: UUID): CreatorRevenueOperation? = null

        override fun existsByPurchaseOperationId(purchaseOperationId: PurchaseOperationId): Boolean = false

        override fun findDueForRetryForUpdate(
            now: Instant,
            limit: Int,
        ): List<CreatorRevenueOperation> =
            listOf(operation)
                .filter { it.status == CreatorRevenueOperationStatus.FAILED }
                .filter { retry -> !requireNotNull(retry.nextRetryAt).isAfter(now) }
                .take(limit)
    }
}
