package com.mnemos.messaging.core.event

object EventHeaders {
    const val CONTENT_TYPE = "content-type"
    const val CONTENT_TYPE_JSON = "application/json"

    const val EVENT_ID = "mnemos-event-id"
    const val EVENT_TYPE = "mnemos-event-type"
    const val EVENT_VERSION = "mnemos-event-version"

    const val PRODUCER = "mnemos-producer"

    const val AGGREGATE_TYPE = "mnemos-aggregate-type"
    const val AGGREGATE_ID = "mnemos-aggregate-id"

    const val OCCURRED_AT = "mnemos-occurred-at"

    const val CORRELATION_ID = "mnemos-correlation-id"
    const val CAUSATION_ID = "mnemos-causation-id"
    const val ACTOR_USER_ID = "mnemos-actor-user-id"
}