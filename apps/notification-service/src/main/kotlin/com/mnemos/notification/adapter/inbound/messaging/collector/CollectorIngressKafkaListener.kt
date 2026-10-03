package com.mnemos.notification.adapter.inbound.messaging.collector

import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import com.mnemos.realtime.contracts.collector.CollectorIngressContract
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CollectorIngressKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [CollectorIngressContract.TOPIC],
        groupId = "${spring.application.name}-collector-ingress",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
