package com.mnemos.realtime.adapter.inbound.http.sse

import com.mnemos.common.web.exception.AppException
import com.mnemos.realtime.adapter.sse.connection.SseConnectionRegistry
import com.mnemos.realtime.application.delivery.usecase.replay.GetRealtimeReplayQuery
import com.mnemos.realtime.application.delivery.usecase.replay.GetRealtimeReplayService
import com.mnemos.realtime.configuration.delivery.RealtimeDeliveryProperties
import com.mnemos.security.CurrentMnemosUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
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
    private val getRealtimeReplayService: GetRealtimeReplayService,
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
        @RequestHeader(REALTIME_SESSION_HEADER, required = false)
        clientSessionId: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SseEmitter {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform")
        response.setHeader("X-Accel-Buffering", "no")

        val userId = currentUser.requireUserId()
        val replay =
            getRealtimeReplayService.execute(
                GetRealtimeReplayQuery(
                    userId = userId,
                    lastEventId = parseUuid(lastEventId),
                ),
            )
        val clientIp = clientIpResolver.resolve(request)
        val emitter = SseEmitter(properties.timeout.toMillis())
        val connectionId =
            connectionRegistry.register(
                userId = userId,
                clientIp = clientIp,
                clientSessionId = parseUuid(clientSessionId),
                emitter = emitter,
                dropSpotlightPresentationMode = replay.dropSpotlightPresentationMode,
            )

        try {
            emitter.send(
                SseEmitter
                    .event()
                    .name("connected")
                    .data(mapOf("userId" to userId)),
            )

            connectionRegistry.completeReplay(
                userId = userId,
                connectionId = connectionId,
                replayEvents = replay.events,
                replayBroadcasts = replay.broadcasts,
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

    @ExceptionHandler(AppException::class)
    fun handleAppException(exception: AppException): ResponseEntity<Void> =
        ResponseEntity
            .status(exception.httpStatus)
            .build()

    private fun parseUuid(value: String?): UUID? =
        try {
            value?.takeIf(String::isNotBlank)?.let(UUID::fromString)
        } catch (_: IllegalArgumentException) {
            null
        }

    private companion object {
        const val LAST_EVENT_ID_HEADER = "Last-Event-ID"
        const val REALTIME_SESSION_HEADER = "X-Mnemos-Realtime-Session"
    }
}
