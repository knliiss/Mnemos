package com.mnemos.wallet.infrastructure.messaging.kafka.transaction

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.wallet.application.transaction.CreateTransactionRequest
import com.mnemos.wallet.application.transaction.ProcessCreateTransactionCommandService
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID

class WalletCreateTransactionCommandHandlerTest {
    private val processService = mock<ProcessCreateTransactionCommandService>()
    private val handler = WalletCreateTransactionCommandHandler(processService)

    @Test
    fun `definition matches create transaction command contract`() {
        assertEquals(WalletCommandContract.TOPIC, handler.definition.topic)
        assertEquals(WalletCommandType.CREATE_TRANSACTION, handler.definition.type)
        assertEquals(WalletCommandContract.VERSION, handler.definition.version)
        assertEquals(WalletCommandContract.AGGREGATE_TYPE, handler.definition.aggregateType)
        assertEquals(WalletCreateTransactionCommand::class.java, handler.definition.payloadType)
    }

    @Test
    fun `handle maps command and propagates metadata`() {
        val messageId = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()
        val command = command()

        handler.handle(
            message(
                messageId = messageId,
                command = command,
                metadata =
                    EventMetadata(
                        correlationId = "correlation-1",
                        actorUserId = actorUserId,
                    ),
            ),
        )

        val requestCaptor = argumentCaptor<CreateTransactionRequest>()
        val metadataCaptor = argumentCaptor<EventMetadata>()

        verify(processService).process(
            request = requestCaptor.capture(),
            metadata = metadataCaptor.capture(),
        )

        val request = requestCaptor.firstValue
        val metadata = metadataCaptor.firstValue

        assertEquals(command.requestId, request.requestId.id)
        assertEquals(command.accountId, request.accountId.id)
        assertEquals(command.currency, request.currency)
        assertEquals(command.type, request.type)
        assertEquals(command.source, request.source)
        assertEquals(command.sourceReferenceId, request.sourceReferenceId.id)
        assertEquals(command.amount, request.amount)

        assertEquals("correlation-1", metadata.correlationId)
        assertEquals(messageId, metadata.causationId)
        assertEquals(actorUserId, metadata.actorUserId)
    }

    @Test
    fun `request id is used as correlation fallback`() {
        val messageId = UUID.randomUUID()
        val command = command()

        handler.handle(
            message(
                messageId = messageId,
                command = command,
                metadata = EventMetadata(),
            ),
        )

        val metadataCaptor = argumentCaptor<EventMetadata>()

        verify(processService).process(
            request = any(),
            metadata = metadataCaptor.capture(),
        )

        assertEquals(command.requestId.toString(), metadataCaptor.firstValue.correlationId)
        assertEquals(messageId, metadataCaptor.firstValue.causationId)
    }

    @Test
    fun `definition rejects aggregate id different from request id`() {
        val command = command()

        val envelope =
            EventEnvelope(
                eventId = UUID.randomUUID(),
                eventType = WalletCommandType.CREATE_TRANSACTION,
                eventVersion = WalletCommandContract.VERSION,
                producer = "bet-service",
                aggregateType = WalletCommandContract.AGGREGATE_TYPE,
                aggregateId = UUID.randomUUID(),
                occurredAt = Instant.parse("2026-08-09T00:00:00Z"),
                metadata = EventMetadata(),
                payload = command,
            )

        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(envelope)
        }
    }

    private fun message(
        messageId: UUID,
        command: WalletCreateTransactionCommand,
        metadata: EventMetadata,
    ) = ConsumedMessage(
        messageId = messageId,
        type = WalletCommandType.CREATE_TRANSACTION,
        version = WalletCommandContract.VERSION,
        producer = "bet-service",
        aggregateType = WalletCommandContract.AGGREGATE_TYPE,
        aggregateId = command.requestId,
        occurredAt = Instant.parse("2026-08-09T00:00:00Z"),
        metadata = metadata,
        payload = command,
    )

    private fun command() =
        WalletCreateTransactionCommand(
            requestId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            currency = Currency.COIN,
            type = TransactionType.CREDIT,
            source = TransactionSource.BET_WIN,
            sourceReferenceId = UUID.randomUUID(),
            amount = 100,
        )
}
