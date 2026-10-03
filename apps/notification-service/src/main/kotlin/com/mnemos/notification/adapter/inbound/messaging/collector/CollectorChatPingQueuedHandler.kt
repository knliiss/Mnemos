package com.mnemos.notification.adapter.inbound.messaging.collector

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.application.ping.ProcessPingNotificationCommand
import com.mnemos.notification.application.ping.ProcessPingNotificationService
import com.mnemos.realtime.contracts.collector.CollectorChatPingQueuedV2
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import com.mnemos.realtime.contracts.collector.CollectorIngressType
import org.springframework.stereotype.Component

@Component
class CollectorChatPingQueuedHandler(
    private val service: ProcessPingNotificationService,
) : KafkaMessageHandler<CollectorChatPingQueuedV2> {
    override val definition =
        kafkaMessageDefinition<CollectorChatPingQueuedV2>(
            CollectorIngressContract.TOPIC,
            CollectorIngressType.CHAT_PING_QUEUED,
            CollectorIngressContract.VERSION,
            CollectorIngressContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != REALTIME_SERVICE) {
                throw KafkaMessageProtocolException("Collector chat pings must be produced by realtime-service")
            }
            if (envelope.aggregateId != envelope.payload.ingressId || envelope.eventId != envelope.payload.ingressId) {
                throw KafkaMessageProtocolException("Collector chat ping identity does not match ingressId")
            }
            if (envelope.occurredAt != envelope.payload.receivedAt) {
                throw KafkaMessageProtocolException("Collector chat ping occurredAt must match receivedAt")
            }
            if (envelope.payload.activeCollectors < 0) {
                throw KafkaMessageProtocolException("Collector chat ping active collector count cannot be negative")
            }
        }

    override val consumer = "notification-service.collector-chat-ping"

    override fun handle(message: ConsumedMessage<CollectorChatPingQueuedV2>) {
        val payload = message.payload
        service.process(
            ProcessPingNotificationCommand(
                messageId = payload.messageId,
                observedAt = payload.observedAt,
                sender = payload.sender,
                message = payload.message,
                text = payload.text,
                mentions = payload.mentions,
            ),
        )
    }

    private companion object {
        const val REALTIME_SERVICE = "realtime-service"
    }
}
