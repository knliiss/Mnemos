package com.mnemos.personalization.domain.purchase

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class PurchaseOperationTest {
    private val now = Instant.parse("2026-08-15T18:30:00Z")

    @Test
    fun `new purchase starts pending without wallet result`() {
        val buyerUserId = UserId.generate()
        val operation =
            PurchaseOperation.create(
                personalizationId = PersonalizationId.generate(),
                buyerUserId = buyerUserId,
                owner = PersonalizationOwner.User(buyerUserId),
                currency = Currency.PRIME,
                amount = 50,
                now = now,
            )

        assertTrue(operation.status == PurchaseOperationStatus.PENDING)
        assertNull(operation.walletTransactionId)
        assertNull(operation.failureReason)
    }

    @Test
    fun `confirmed purchase without wallet transaction is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            operation(
                status = PurchaseOperationStatus.CONFIRMED,
                walletTransactionId = null,
                failureReason = null,
            )
        }
    }

    @Test
    fun `failed purchase without reason is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            operation(
                status = PurchaseOperationStatus.FAILED,
                walletTransactionId = null,
                failureReason = null,
            )
        }
    }

    @Test
    fun `failure reason longer than persistence limit is rejected`() {
        val buyerUserId = UserId.generate()
        val operation =
            PurchaseOperation.create(
                personalizationId = PersonalizationId.generate(),
                buyerUserId = buyerUserId,
                owner = PersonalizationOwner.User(buyerUserId),
                currency = Currency.PRIME,
                amount = 50,
                now = now,
            )

        assertThrows(IllegalArgumentException::class.java) {
            operation.fail("x".repeat(129))
        }
    }

    @Test
    fun `coin purchase with user owner is rejected`() {
        val buyerUserId = UserId.generate()

        assertThrows(IllegalArgumentException::class.java) {
            PurchaseOperation.create(
                personalizationId = PersonalizationId.generate(),
                buyerUserId = buyerUserId,
                owner = PersonalizationOwner.User(buyerUserId),
                currency = Currency.COIN,
                amount = 500,
                now = now,
            )
        }
    }

    @Test
    fun `user owned purchase for another user is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            PurchaseOperation.create(
                personalizationId = PersonalizationId.generate(),
                buyerUserId = UserId.generate(),
                owner = PersonalizationOwner.User(UserId.generate()),
                currency = Currency.PRIME,
                amount = 50,
                now = now,
            )
        }
    }

    private fun operation(
        status: PurchaseOperationStatus,
        walletTransactionId: UUID?,
        failureReason: String?,
    ): PurchaseOperation {
        val buyerUserId = UserId.generate()

        return PurchaseOperation(
            id = PurchaseOperationId.generate(),
            requestId = UUID.fromString("019c1177-10c0-7000-8000-000000000001"),
            personalizationId = PersonalizationId.generate(),
            buyerUserId = buyerUserId,
            owner = PersonalizationOwner.User(buyerUserId),
            currency = Currency.PRIME,
            amount = 50,
            createdAt = now,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
        )
    }
}
