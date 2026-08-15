package com.mnemos.realtime.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.application.delivery.model.WalletTransactionFailedPayload
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventService
import com.mnemos.realtime.support.RecordingRealtimeEventSink
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class WalletRealtimeHandlersTest {
    private val eventSink = RecordingRealtimeEventSink()
    private val eventService =
        PublishRealtimeEventService(
            replayStore = eventSink,
            fanout = eventSink,
            telemetry = eventSink,
            clock = Clock.fixed(Instant.parse("2026-08-15T10:10:00Z"), ZoneOffset.UTC),
        )

    @Test
    fun `publishes a completed debit as a user realtime event`() {
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000030")
        val requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000031")
        val transactionId = UUID.fromString("019c1129-ef54-7000-8000-000000000032")
        val operationId = UUID.fromString("019c1129-ef54-7000-8000-000000000033")
        val messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000034")
        val occurredAt = Instant.parse("2026-08-15T10:00:00Z")
        val event =
            WalletTransactionCompletedEvent(
                requestId = requestId,
                transactionId = transactionId,
                owner = WalletOwner(WalletOwnerType.ACCOUNT, userId),
                currency = Currency.COIN,
                type = TransactionType.DEBIT,
                source = TransactionSource.BET_PLACE,
                sourceReferenceId = operationId,
                amount = 500,
                occurredAt = occurredAt,
            )

        WalletTransactionCompletedHandler(eventService).handle(
            consumedMessage(
                messageId = messageId,
                userId = userId,
                aggregateId = requestId,
                type = WalletEventType.TRANSACTION_COMPLETED,
                payload = event,
            ),
        )

        val published = eventSink.persistedEvents.single()

        assertEquals(messageId, published.id)
        assertEquals(userId, published.userId)
        assertEquals(RealtimeEventType.WALLET_TRANSACTION_COMPLETED, published.type)
        assertEquals(null, published.operationId)
        val realtimePayload = published.payload as WalletTransactionCompletedPayload

        assertEquals(transactionId, realtimePayload.transactionId)
        assertEquals(operationId, realtimePayload.sourceReferenceId)
        assertEquals(500L, realtimePayload.amount)
        assertEquals(listOf(published), eventSink.broadcastEvents)
    }

    @Test
    fun `publishes a failed debit with its failure reason`() {
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000040")
        val requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000041")
        val operationId = UUID.fromString("019c1129-ef54-7000-8000-000000000042")
        val event =
            WalletTransactionFailedEvent(
                requestId = requestId,
                owner = WalletOwner(WalletOwnerType.ACCOUNT, userId),
                currency = Currency.COIN,
                type = TransactionType.DEBIT,
                source = TransactionSource.BET_PLACE,
                sourceReferenceId = operationId,
                amount = 750,
                reason = WalletTransactionFailureReason.INSUFFICIENT_BALANCE,
                occurredAt = Instant.parse("2026-08-15T10:01:00Z"),
            )

        WalletTransactionFailedHandler(eventService).handle(
            consumedMessage(
                messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000043"),
                userId = userId,
                aggregateId = requestId,
                type = WalletEventType.TRANSACTION_FAILED,
                payload = event,
            ),
        )

        val published = eventSink.persistedEvents.single()

        assertEquals(RealtimeEventType.WALLET_TRANSACTION_FAILED, published.type)
        assertEquals(null, published.operationId)
        val realtimePayload = published.payload as WalletTransactionFailedPayload

        assertEquals(operationId, realtimePayload.sourceReferenceId)
        assertEquals("INSUFFICIENT_BALANCE", realtimePayload.reason)
    }

    @Test
    fun `ignores credits and rejects debit results without an actor`() {
        val ownerId = UUID.fromString("019c1129-ef54-7000-8000-000000000050")
        val requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000051")
        val operationId = UUID.fromString("019c1129-ef54-7000-8000-000000000052")
        val credit =
            WalletTransactionCompletedEvent(
                requestId = requestId,
                transactionId = UUID.fromString("019c1129-ef54-7000-8000-000000000053"),
                owner = WalletOwner(WalletOwnerType.ACCOUNT, ownerId),
                currency = Currency.COIN,
                type = TransactionType.CREDIT,
                source = TransactionSource.BET_REFUND,
                sourceReferenceId = operationId,
                amount = 500,
                occurredAt = Instant.parse("2026-08-15T10:02:00Z"),
            )
        val debit = credit.copy(type = TransactionType.DEBIT)
        val handler = WalletTransactionCompletedHandler(eventService)

        handler.handle(
            consumedMessage(
                messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000054"),
                userId = null,
                aggregateId = requestId,
                type = WalletEventType.TRANSACTION_COMPLETED,
                payload = credit,
            ),
        )

        assertEquals(emptyList<Any>(), eventSink.persistedEvents)
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.handle(
                consumedMessage(
                    messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000055"),
                    userId = null,
                    aggregateId = requestId,
                    type = WalletEventType.TRANSACTION_COMPLETED,
                    payload = debit,
                ),
            )
        }
    }

    private fun <T : Any> consumedMessage(
        messageId: UUID,
        userId: UUID?,
        aggregateId: UUID,
        type: String,
        payload: T,
    ): ConsumedMessage<T> =
        ConsumedMessage(
            messageId = messageId,
            type = type,
            version = WalletEventContract.VERSION,
            producer = "wallet-service",
            aggregateType = WalletEventContract.AGGREGATE_TYPE,
            aggregateId = aggregateId,
            occurredAt = Instant.parse("2026-08-15T10:00:00Z"),
            metadata = EventMetadata(actorUserId = userId),
            payload = payload,
        )
}
