package com.mnemos.realtime.contracts.collector

import java.util.UUID

object CollectorIngressContract {
    const val TOPIC = "mnemos.collector.ingress.v2"
    const val VERSION = 2
    const val AGGREGATE_TYPE = "COLLECTOR_INGRESS"

    val PRESENCE_AGGREGATE_ID: UUID =
        UUID.fromString("dcd61e9a-b29f-5ea6-8b68-a32f5250da2d")
}

object CollectorIngressType {
    const val REPORT_QUEUED = "collector.report.queued"
    const val CHAT_PING_QUEUED = "collector.chat-ping.queued"
    const val PRESENCE_CHANGED = "collector.presence.changed"
}

object CollectorIngressTopicPolicy {
    const val PARTITIONS = 1
    const val RETENTION_MS = "86400000"
    const val RETENTION_BYTES = "268435456"
    const val SEGMENT_MS = "300000"
    const val SEGMENT_BYTES = "67108864"
    const val DEAD_LETTER_RETENTION_MS = "604800000"
    const val DEAD_LETTER_RETENTION_BYTES = "67108864"
}
