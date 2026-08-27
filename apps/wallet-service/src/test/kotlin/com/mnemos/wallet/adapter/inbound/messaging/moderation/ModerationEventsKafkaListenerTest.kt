package com.mnemos.wallet.adapter.inbound.messaging.moderation

import com.mnemos.moderation.contracts.ModerationEventContract
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.kafka.annotation.KafkaListener

class ModerationEventsKafkaListenerTest {
    @Test
    fun `subscribes wallet service to moderation events`() {
        val method =
            ModerationEventsKafkaListener::class.java.getDeclaredMethod(
                "handle",
                ConsumerRecord::class.java,
            )
        val annotation = method.getAnnotation(KafkaListener::class.java)

        assertNotNull(annotation)
        assertArrayEquals(arrayOf(ModerationEventContract.TOPIC), annotation.topics)
        assertEquals("\${spring.application.name}-moderation-events", annotation.groupId)
    }
}
