package com.mnemos.realtime.adapter.inbound.messaging.wallet

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.realtime.application.delivery.model.RealtimeEventType
import com.mnemos.realtime.application.delivery.model.WalletRealtimeOwner
import com.mnemos.realtime.application.delivery.model.WalletRealtimeOwnerType
import com.mnemos.realtime.application.delivery.model.WalletTransactionCompletedPayload
import com.mnemos.realtime.application.delivery.model.WalletTransactionFailedPayload
import com.mnemos.realtime.application.delivery.port.out.WalletRealtimeRecipientResolver
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
    fun `publishes a completed debit to every account member`() {
        val accountId = UUID.fromString("019c1129-ef54-7000-8000-000000000030")
        val firstUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000031")
        val secondUserId = UUID.fromString("019c1129-ef54-7000-8000-000000000032")
        val requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000033")
        val transactionId = UUID.fromString("019c1129-ef54-7000-8000-000000000034")
        val operationId = UUID.fromString("019c1129-ef54-7000-8000-000000000035")
        val messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000036")
        val occurredAt = Instant.parse("2026-08-15T10:00:00Z")
        val event =
            WalletTransactionCompletedEvent(
                requestId = requestId,
                transactionId = transactionId,
                owner = WalletOwner(WalletOwnerType.ACCOUNT, accountId),
                currency = Currency.COIN,
                type = TransactionType.DEBIT,
                source = TransactionSource.BET_PLACE,
                sourceReferenceId = operationId,
                amount = 500,
                occurredAt = occurredAt,
            )
        val recipientResolver =
            StubWalletRealtimeRecipientResolver(
                accountRecipients = setOf(firstUserId, secondUserId),
            )

        WalletTransactionCompletedHandler(eventService, recipientResolver).handle(
            consumedMessage(
                messageId = messageId,
                userId = firstUserId,
                aggregateId = requestId,
                type = WalletEventType.TRANSACTION_COMPLETED,
                payload = event,
            ),
        )

        assertEquals(setOf(firstUserId, secondUserId), eventSink.persistedEvents.map { it.userId }.toSet())
        assertEquals(2, eventSink.persistedEvents.size)
        eventSink.persistedEvents.forEach { published ->
            assertEquals(messageId, published.id)
            assertEquals(RealtimeEventType.WALLET_TRANSACTION_COMPLETED, published.type)
            assertEquals(null, published.operationId)
            val realtimePayload = published.payload as WalletTransactionCompletedPayload

            assertEquals(accountId, realtimePayload.ownerId)
            assertEquals(transactionId, realtimePayload.transactionId)
            assertEquals(operationId, realtimePayload.sourceReferenceId)
            assertEquals(500L, realtimePayload.amount)
        }
    }

    @Test
    fun `publishes a user credit to the wallet owner without an actor`() {
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000040")
        val requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000041")
        val transactionId = UUID.fromString("019c1129-ef54-7000-8000-000000000042")
        val event =
            WalletTransactionCompletedEvent(
                requestId = requestId,
                transactionId = transactionId,
                owner = WalletOwner(WalletOwnerType.USER, userId),
                currency = Currency.PRIME,
                type = TransactionType.CREDIT,
                source = TransactionSource.ADMIN_ADJUSTMENT,
                sourceReferenceId = UUID.fromString("019c1129-ef54-7000-8000-000000000043"),
                amount = 25,
                occurredAt = Instant.parse("2026-08-15T10:01:00Z"),
            )

        WalletTransactionCompletedHandler(eventService, StubWalletRealtimeRecipientResolver()).handle(
            consumedMessage(
                messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000044"),
                userId = null,
                aggregateId = requestId,
                type = WalletEventType.TRANSACTION_COMPLETED,
                payload = event,
            ),
        )

        val published = eventSink.persistedEvents.single()

        assertEquals(userId, published.userId)
        val payload = published.payload as WalletTransactionCompletedPayload

        assertEquals("USER", payload.ownerType)
        assertEquals("PRIME", payload.currency)
        assertEquals(25L, payload.amount)
    }

    @Test
    fun `publishes a failed debit with its failure reason`() {
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000050")
        val requestId = UUID.fromString("019c1129-ef54-7000-8000-000000000051")
        val operationId = UUID.fromString("019c1129-ef54-7000-8000-000000000052")
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
                occurredAt = Instant.parse("2026-08-15T10:02:00Z"),
            )

        WalletTransactionFailedHandler(eventService).handle(
            consumedMessage(
                messageId = UUID.fromString("019c1129-ef54-7000-8000-000000000053"),
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

    private class StubWalletRealtimeRecipientResolver(
        private val accountRecipients: Set<UUID> = emptySet(),
    ) : WalletRealtimeRecipientResolver {
        override fun resolve(owner: WalletRealtimeOwner): Set<UUID> =
            when (owner.type) {
                WalletRealtimeOwnerType.USER -> setOf(owner.id)
                WalletRealtimeOwnerType.ACCOUNT -> accountRecipients
            }
    }
}
