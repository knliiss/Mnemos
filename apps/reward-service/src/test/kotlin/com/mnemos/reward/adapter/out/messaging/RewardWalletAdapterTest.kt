package com.mnemos.reward.adapter.out.messaging

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class RewardWalletAdapterTest {
    @Test
    fun `daily coin credit uses stable idempotency identifiers for account and date`() {
        val publisher = RecordingIntegrationEventPublisher()
        val adapter = RewardWalletAdapter(publisher)
        val accountId = AccountId(UUID.fromString("0198a000-0000-7000-8000-000000000001"))
        val rewardDate = LocalDate.of(2026, 8, 22)

        adapter.creditDailyCoin(
            accountId = accountId,
            amount = 60,
            rewardDate = rewardDate,
        )
        adapter.creditDailyCoin(
            accountId = accountId,
            amount = 60,
            rewardDate = rewardDate,
        )

        val first = publisher.commands[0]
        val replay = publisher.commands[1]

        assertEquals(first.requestId, replay.requestId)
        assertEquals(first.sourceReferenceId, replay.sourceReferenceId)
    }

    @Test
    fun `daily coin credit changes idempotency identifiers for next reward date`() {
        val publisher = RecordingIntegrationEventPublisher()
        val adapter = RewardWalletAdapter(publisher)
        val accountId = AccountId(UUID.fromString("0198a000-0000-7000-8000-000000000001"))

        adapter.creditDailyCoin(
            accountId = accountId,
            amount = 60,
            rewardDate = LocalDate.of(2026, 8, 22),
        )
        adapter.creditDailyCoin(
            accountId = accountId,
            amount = 60,
            rewardDate = LocalDate.of(2026, 8, 23),
        )

        val first = publisher.commands[0]
        val nextDay = publisher.commands[1]

        assertNotEquals(first.requestId, nextDay.requestId)
        assertNotEquals(first.sourceReferenceId, nextDay.sourceReferenceId)
    }

    private class RecordingIntegrationEventPublisher : IntegrationEventPublisher {
        val commands = mutableListOf<WalletCreateTransactionCommand>()

        override fun <T : Any> publish(
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            commands += event.payload as WalletCreateTransactionCommand

            return UUID.fromString("0198a000-0000-7000-8000-000000000002")
        }
    }
}
