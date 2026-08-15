package com.mnemos.realtime.adapter.out.metrics.delivery

import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.port.out.RealtimeDeliveryTelemetry
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@Component
class MicrometerRealtimeDeliveryTelemetry(
    private val meterRegistry: MeterRegistry,
    private val clock: Clock,
) : RealtimeDeliveryTelemetry {
    private val activeConnections = AtomicInteger()

    init {
        Gauge
            .builder(
                "mnemos.realtime.sse.connections",
                activeConnections,
                { value -> value.get().toDouble() },
            ).description("Current number of local SSE connections")
            .register(meterRegistry)
    }

    override fun recordPublishSuccess(
        event: RealtimeEvent,
        duration: Duration,
    ) {
        meterRegistry
            .timer(
                "mnemos.realtime.publish.duration",
                EVENT_TYPE_TAG,
                event.type.eventName,
                RESULT_TAG,
                SUCCESS,
            ).record(duration)
    }

    override fun recordPublishFailure(
        event: RealtimeEvent,
        duration: Duration,
    ) {
        meterRegistry
            .timer(
                "mnemos.realtime.publish.duration",
                EVENT_TYPE_TAG,
                event.type.eventName,
                RESULT_TAG,
                FAILURE,
            ).record(duration)
    }

    override fun recordStreamDelivery(event: RealtimeEvent) {
        val age = Duration.between(event.occurredAt, clock.instant())

        meterRegistry
            .timer(
                "mnemos.realtime.delivery.age",
                EVENT_TYPE_TAG,
                event.type.eventName,
            ).record(age.coerceAtLeast(Duration.ZERO))
    }

    override fun connectionOpened() {
        activeConnections.incrementAndGet()
    }

    override fun connectionClosed() {
        activeConnections.updateAndGet { current -> maxOf(0, current - 1) }
    }

    override fun recordSseSendFailure() {
        meterRegistry
            .counter("mnemos.realtime.sse.send.failures")
            .increment()
    }

    private companion object {
        const val EVENT_TYPE_TAG = "event.type"
        const val RESULT_TAG = "result"
        const val SUCCESS = "success"
        const val FAILURE = "failure"
    }
}
