package com.mnemos.event.adapter.inbound.messaging.playeridentity

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PlayerDirectoryEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [PlayerDirectoryEventContract.TOPIC],
        groupId = "\${spring.application.name}-player-directory-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
