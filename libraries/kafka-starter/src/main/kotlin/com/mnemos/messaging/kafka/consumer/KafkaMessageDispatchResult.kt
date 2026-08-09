package com.mnemos.messaging.kafka.consumer

enum class KafkaMessageDispatchResult {
    PROCESSED,
    DUPLICATE,
    IGNORED,
}