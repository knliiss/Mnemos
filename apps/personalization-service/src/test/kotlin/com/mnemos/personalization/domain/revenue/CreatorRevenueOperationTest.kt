package com.mnemos.personalization.domain.revenue

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationId
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CreatorRevenueOperationTest {
    private val now = Instant.parse("2026-08-15T18:45:00Z")

    @Test
    fun `new creator revenue starts pending without wallet result`() {
        val operation =
            CreatorRevenueOperation.create(
                purchaseOperationId = PurchaseOperationId.generate(),
                creatorUserId = UserId.generate(),
                amountPrime = 10,
                now = now,
            )

        assertEquals(CreatorRevenueOperationStatus.PENDING, operation.status)
        assertNull(operation.walletTransactionId)
        assertNull(operation.failureReason)
        assertNull(operation.nextRetryAt)
    }

    @Test
    fun `confirmed creator revenue without wallet transaction is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            operation(
                status = CreatorRevenueOperationStatus.CONFIRMED,
                walletTransactionId = null,
                failureReason = null,
                nextRetryAt = null,
            )
        }
    }

    @Test
    fun `failed creator revenue without retry time is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            operation(
                status = CreatorRevenueOperationStatus.FAILED,
                walletTransactionId = null,
                failureReason = "TEMPORARY_FAILURE",
                nextRetryAt = null,
            )
        }
    }

    @Test
    fun `retry resets failure state and changes wallet request id`() {
        val operation =
            CreatorRevenueOperation.create(
                purchaseOperationId = PurchaseOperationId.generate(),
                creatorUserId = UserId.generate(),
                amountPrime = 10,
                now = now.minusSeconds(60),
            )
        val previousRequestId = operation.requestId

        operation.fail(
            reason = "TEMPORARY_FAILURE",
            retryAt = now.minusSeconds(1),
        )
        operation.prepareRetry(now)

        assertEquals(CreatorRevenueOperationStatus.PENDING, operation.status)
        assertNull(operation.walletTransactionId)
        assertNull(operation.failureReason)
        assertNull(operation.nextRetryAt)
        assertNotEquals(previousRequestId, operation.requestId)
    }

    private fun operation(
        status: CreatorRevenueOperationStatus,
        walletTransactionId: UUID?,
        failureReason: String?,
        nextRetryAt: Instant?,
    ): CreatorRevenueOperation =
        CreatorRevenueOperation(
            id = CreatorRevenueOperationId.generate(),
            requestId = UUID.fromString("019c1177-10c0-7000-8000-000000000002"),
            purchaseOperationId = PurchaseOperationId.generate(),
            creatorUserId = UserId.generate(),
            amountPrime = 10,
            createdAt = now,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            nextRetryAt = nextRetryAt,
        )
}
