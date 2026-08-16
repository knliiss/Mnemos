package com.mnemos.social.adapter.inbound.messaging.wallet

import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.social.adapter.inbound.messaging.wallet.SpotlightWalletResultMessageMapper.toSpotlightResult
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SpotlightWalletResultMessageMapperTest {
    @Test
    fun `maps valid spotlight purchase result`() {
        val event = event()

        val result = requireNotNull(event.toSpotlightResult())

        assertEquals(event.requestId, result.requestId)
        assertEquals(event.sourceReferenceId, result.spotlightId)
        assertEquals(event.owner.id, result.ownerUserId)
        assertEquals(event.amount, result.amount)
    }

    @Test
    fun `ignores wallet event for another transaction source`() {
        val event = event().copy(source = TransactionSource.PERSONALIZATION_PURCHASE)

        assertNull(event.toSpotlightResult())
    }

    @Test
    fun `rejects malformed spotlight wallet result shape`() {
        val wrongCurrency = event().copy(currency = Currency.COIN)
        val wrongType = event().copy(type = TransactionType.CREDIT)
        val wrongOwner =
            event().copy(
                owner =
                    WalletOwner(
                        type = WalletOwnerType.ACCOUNT,
                        id = UUID.fromString("019c1ab0-8800-7000-8000-000000000009"),
                    ),
            )

        assertThrows(KafkaMessageProtocolException::class.java) {
            wrongCurrency.toSpotlightResult()
        }
        assertThrows(KafkaMessageProtocolException::class.java) {
            wrongType.toSpotlightResult()
        }
        assertThrows(KafkaMessageProtocolException::class.java) {
            wrongOwner.toSpotlightResult()
        }
    }

    private fun event(): WalletTransactionCompletedEvent =
        WalletTransactionCompletedEvent(
            requestId = UUID.fromString("019c1ab0-8800-7000-8000-000000000001"),
            transactionId = UUID.fromString("019c1ab0-8800-7000-8000-000000000002"),
            owner =
                WalletOwner(
                    type = WalletOwnerType.USER,
                    id = UUID.fromString("019c1ab0-8800-7000-8000-000000000003"),
                ),
            currency = Currency.PRIME,
            type = TransactionType.DEBIT,
            source = TransactionSource.SPOTLIGHT_PURCHASE,
            sourceReferenceId = UUID.fromString("019c1ab0-8800-7000-8000-000000000004"),
            amount = 25,
            occurredAt = Instant.parse("2026-08-16T00:00:01Z"),
        )
}
