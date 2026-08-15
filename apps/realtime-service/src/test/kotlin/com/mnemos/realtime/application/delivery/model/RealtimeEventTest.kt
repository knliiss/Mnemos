package com.mnemos.realtime.application.delivery.model

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class RealtimeEventTest {
    @Test
    fun `rejects a payload whose type differs from the envelope`() {
        assertThrows(IllegalArgumentException::class.java) {
            event(
                type = RealtimeEventType.WALLET_TRANSACTION_FAILED,
                operationId = null,
                payload = walletCompletedPayload(),
            )
        }
    }

    @Test
    fun `rejects a generic wallet event with a domain operation id`() {
        assertThrows(IllegalArgumentException::class.java) {
            event(
                type = RealtimeEventType.WALLET_TRANSACTION_COMPLETED,
                operationId = UUID.randomUUID(),
                payload = walletCompletedPayload(),
            )
        }
    }

    @Test
    fun `rejects a bet result without its operation id`() {
        assertThrows(IllegalArgumentException::class.java) {
            event(
                type = RealtimeEventType.BET_OPERATION_COMPLETED,
                operationId = null,
                payload =
                    BetOperationCompletedPayload(
                        resourceType = "EVENT_BET",
                        resourceId = UUID.randomUUID(),
                        status = "CONFIRMED",
                        amount = 500,
                        walletTransactionId = UUID.randomUUID(),
                        failureReason = null,
                    ),
            )
        }
    }

    private fun event(
        type: RealtimeEventType,
        operationId: UUID?,
        payload: RealtimeEventPayload,
    ): RealtimeEvent =
        RealtimeEvent(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            type = type,
            occurredAt = Instant.parse("2026-08-15T10:00:00Z"),
            operationId = operationId,
            payload = payload,
        )

    private fun walletCompletedPayload(): WalletTransactionCompletedPayload =
        WalletTransactionCompletedPayload(
            requestId = UUID.randomUUID(),
            transactionId = UUID.randomUUID(),
            ownerType = "ACCOUNT",
            ownerId = UUID.randomUUID(),
            currency = "COIN",
            source = "BET_PLACE",
            sourceReferenceId = UUID.randomUUID(),
            amount = 500,
        )
}
