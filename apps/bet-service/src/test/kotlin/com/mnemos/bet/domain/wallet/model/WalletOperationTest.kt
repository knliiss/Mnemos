package com.mnemos.bet.domain.wallet.model

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class WalletOperationTest {
    @Test
    fun `failed credit retry creates new time ordered ids`() {
        val createdAt = Instant.parse("2026-08-13T16:00:00Z")
        val operation =
            WalletOperation.create(
                accountId = AccountId.generate(),
                amount = 1_000,
                kind = WalletOperationKind.REFUND,
                eventBetId = EventBetId.generate(),
                eventBetResolutionId = null,
                raceParticipantId = null,
                createdAt = createdAt,
                eligibleAt = createdAt,
            )

        operation.fail()

        val retry = operation.retry(createdAt.plusSeconds(60))

        assertNotEquals(operation.id, retry.id)
        assertNotEquals(operation.requestId, retry.requestId)
        assertEquals(7, retry.id.id.version())
        assertEquals(7, retry.requestId.id.version())
        assertEquals(WalletOperationStatus.PENDING, retry.status)
        assertEquals(operation.amount, retry.amount)
        assertEquals(operation.kind, retry.kind)
    }

    @Test
    fun `stake operation cannot be retried`() {
        val createdAt = Instant.parse("2026-08-13T16:00:00Z")
        val operation =
            WalletOperation.create(
                accountId = AccountId.generate(),
                amount = 1_000,
                kind = WalletOperationKind.STAKE,
                eventBetId = EventBetId.generate(),
                eventBetResolutionId = null,
                raceParticipantId = null,
                createdAt = createdAt,
                eligibleAt = createdAt,
            )

        operation.fail()

        assertThrows(IllegalStateException::class.java) {
            operation.retry(createdAt.plusSeconds(60))
        }
    }
}
