package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.realtime.adapter.sse.connection.SseConnectionRegistry
import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationPreferenceStore
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventReplayStore
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import com.mnemos.security.CurrentMnemosUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@RestController
@RequestMapping("/api/v1/realtime")
class SseController(
    private val currentUser: CurrentMnemosUser,
    private val replayStore: RealtimeEventReplayStore,
    private val broadcastReplayStore: RealtimeBroadcastReplayStore,
    private val presentationPreferenceStore: DropSpotlightPresentationPreferenceStore,
    private val connectionRegistry: SseConnectionRegistry,
    private val clientIpResolver: SseClientIpResolver,
    private val properties: RealtimeDeliveryProperties,
) {
    @GetMapping(
        path = ["/events"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun events(
        @RequestHeader(LAST_EVENT_ID_HEADER, required = false)
        lastEventId: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SseEmitter {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform")
        response.setHeader("X-Accel-Buffering", "no")

        val userId = currentUser.requireUserId()
        val clientIp = clientIpResolver.resolve(request)
        val emitter = SseEmitter(properties.timeout.toMillis())
        val presentationMode = presentationPreferenceStore.find(userId)
        val connectionId =
            connectionRegistry.register(
                userId = userId,
                clientIp = clientIp,
                emitter = emitter,
                dropSpotlightPresentationMode = presentationMode,
            )

        try {
            emitter.send(
                SseEmitter
                    .event()
                    .name("connected")
                    .data(mapOf("userId" to userId)),
            )

            val replayEvents =
                parseEventId(lastEventId)
                    ?.let { eventId ->
                        replayStore.findAfter(
                            userId = userId,
                            lastEventId = eventId,
                        )
                    }.orEmpty()
            val replayBroadcasts = broadcastReplayStore.findPending(userId)

            connectionRegistry.completeReplay(
                userId = userId,
                connectionId = connectionId,
                replayEvents = replayEvents,
                replayBroadcasts = replayBroadcasts,
            )
        } catch (exception: Exception) {
            connectionRegistry.unregister(
                userId = userId,
                connectionId = connectionId,
            )
            emitter.completeWithError(exception)
        }

        return emitter
    }

    private fun parseEventId(value: String?): UUID? =
        try {
            value?.takeIf(String::isNotBlank)?.let(UUID::fromString)
        } catch (_: IllegalArgumentException) {
            null
        }

    private companion object {
        const val LAST_EVENT_ID_HEADER = "Last-Event-ID"
    }
}
