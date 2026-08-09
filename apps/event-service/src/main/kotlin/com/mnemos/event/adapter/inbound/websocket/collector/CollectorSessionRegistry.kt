package com.mnemos.event.adapter.inbound.websocket.collector

import com.mnemos.event.application.collector.port.out.ActiveCollectorProvider
import com.mnemos.event.domain.verification.model.CollectorId
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class CollectorSessionRegistry : ActiveCollectorProvider {
    private val collectorBySessionId =
        ConcurrentHashMap<String, CollectorId>()

    private val sessionByCollector =
        ConcurrentHashMap<CollectorId, WebSocketSession>()

    private val lastActivityBySessionId =
        ConcurrentHashMap<String, Instant>()

    @Synchronized
    fun register(
        session: WebSocketSession,
        collectorId: CollectorId,
    ) {
        val previousSession =
            sessionByCollector.put(
                collectorId,
                session,
            )

        collectorBySessionId[session.id] =
            collectorId

        lastActivityBySessionId[session.id] =
            Instant.now()

        if (
            previousSession != null &&
            previousSession.id != session.id
        ) {
            collectorBySessionId.remove(
                previousSession.id,
            )

            lastActivityBySessionId.remove(
                previousSession.id,
            )

            if (previousSession.isOpen) {
                previousSession.close(
                    CloseStatus.NORMAL,
                )
            }
        }
    }

    fun unregister(session: WebSocketSession) {
        val collectorId =
            collectorBySessionId.remove(
                session.id,
            ) ?: return

        lastActivityBySessionId.remove(
            session.id,
        )

        sessionByCollector.remove(
            collectorId,
            session,
        )
    }

    fun touch(session: WebSocketSession) {
        if (
            collectorBySessionId.containsKey(
                session.id,
            )
        ) {
            lastActivityBySessionId[session.id] =
                Instant.now()
        }
    }

    fun findCollectorId(session: WebSocketSession): CollectorId? = collectorBySessionId[session.id]

    fun activeSessions(): List<WebSocketSession> = sessionByCollector.values.toList()

    fun isStale(
        session: WebSocketSession,
        now: Instant,
        timeout: Duration,
    ): Boolean {
        val lastActivity =
            lastActivityBySessionId[session.id]
                ?: return true

        return !lastActivity
            .plus(timeout)
            .isAfter(now)
    }

    override fun activeCollectorCount(): Int = sessionByCollector.size
}
