package com.mnemos.messaging.kafka.consumer

import com.mnemos.messaging.core.message.ConsumedMessage

interface KafkaMessageHandler<T : Any> {
    val definition: KafkaMessageDefinition<T>
    val consumer: String

    fun handle(message: ConsumedMessage<T>)
}