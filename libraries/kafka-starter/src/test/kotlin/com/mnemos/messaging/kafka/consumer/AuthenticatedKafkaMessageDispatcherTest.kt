package com.mnemos.messaging.kafka.consumer

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.security.KafkaMessageAuthenticator
import com.mnemos.messaging.kafka.serialization.KafkaEventEnvelopeReader
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.security.KeyPairGenerator
import java.util.Base64

class AuthenticatedKafkaMessageDispatcherTest {
    @Test
    fun `unsigned message is rejected before handler`() {
        val handler = RecordingHandler()
        val dispatcher =
            KafkaMessageDispatcher(
                envelopeReader = mock<KafkaEventEnvelopeReader>(),
                messageProcessor =
                    KafkaMessageProcessor { _, _, action ->
                        action()
                        KafkaMessageProcessingResult.PROCESSED
                    },
                handlers = listOf(handler),
                messageAuthenticator = authenticator(),
            )
        val record =
            ConsumerRecord(
                "mnemos.account.events.v1",
                0,
                0L,
                "account-1",
                "{\"eventType\":\"account.created\"}",
            )

        assertThrows(KafkaMessageProtocolException::class.java) {
            dispatcher.dispatch(record)
        }

        assertEquals(0, handler.messages.size)
    }

    private fun authenticator(): KafkaMessageAuthenticator {
        val keyPair =
            KeyPairGenerator
                .getInstance("Ed25519")
                .generateKeyPair()

        return KafkaMessageAuthenticator.fromEncodedKeys(
            serviceName = "account-service",
            encodedPrivateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded),
            encodedTrustedPublicKeys =
                mapOf(
                    "account-service" to Base64.getEncoder().encodeToString(keyPair.public.encoded),
                ),
        )
    }

    private class RecordingHandler : KafkaMessageHandler<TestPayload> {
        override val consumer = "wallet-service.account-created"
        override val definition =
            KafkaMessageDefinition(
                topic = "mnemos.account.events.v1",
                type = "account.created",
                version = 1,
                aggregateType = "ACCOUNT",
                payloadType = TestPayload::class.java,
            )
        val messages = mutableListOf<ConsumedMessage<TestPayload>>()

        override fun handle(message: ConsumedMessage<TestPayload>) {
            messages += message
        }
    }

    private data class TestPayload(
        val nickname: String,
    )
}
