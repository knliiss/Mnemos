package com.mnemos.moderation.domain.wallet

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentStatus
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class WalletAdjustmentOperationTest {
    private val now = Instant.parse("2026-08-17T12:00:00Z")

    @Test
    fun `coin adjustment requires account target`() {
        assertThrows(IllegalArgumentException::class.java) {
            WalletAdjustmentOperation.create(
                targetType = WalletOwnerType.USER,
                targetId = UserId.generate().id,
                actorUserId = UserId.generate(),
                currency = Currency.COIN,
                type = TransactionType.CREDIT,
                amount = 100,
                reason = "Admin compensation",
                now = now,
            )
        }
    }

    @Test
    fun `prime adjustment requires user target`() {
        assertThrows(IllegalArgumentException::class.java) {
            WalletAdjustmentOperation.create(
                targetType = WalletOwnerType.ACCOUNT,
                targetId = AccountId.generate().id,
                actorUserId = UserId.generate(),
                currency = Currency.PRIME,
                type = TransactionType.CREDIT,
                amount = 10,
                reason = "Admin compensation",
                now = now,
            )
        }
    }

    @Test
    fun `completed adjustment stores wallet transaction`() {
        val operation =
            WalletAdjustmentOperation.create(
                targetType = WalletOwnerType.ACCOUNT,
                targetId = AccountId.generate().id,
                actorUserId = UserId.generate(),
                currency = Currency.COIN,
                type = TransactionType.CREDIT,
                amount = 100,
                reason = " Admin compensation ",
                now = now,
            )
        val transactionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")

        operation.complete(transactionId)

        assertEquals(WalletAdjustmentStatus.COMPLETED, operation.status)
        assertEquals(transactionId, operation.walletTransactionId)
        assertEquals("Admin compensation", operation.reason)
    }

    @Test
    fun `failed adjustment records bounded failure reason`() {
        val operation =
            WalletAdjustmentOperation.create(
                targetType = WalletOwnerType.USER,
                targetId = UserId.generate().id,
                actorUserId = UserId.generate(),
                currency = Currency.PRIME,
                type = TransactionType.DEBIT,
                amount = 10,
                reason = "Fraud correction",
                now = now,
            )
        val failureReason = "x".repeat(200)

        operation.fail(failureReason)

        assertEquals(WalletAdjustmentStatus.FAILED, operation.status)
        assertTrue(requireNotNull(operation.failureReason).length <= 128)
    }
}
