package com.mnemos.realtime.adapter.inbound.scheduling.collector

import com.mnemos.realtime.adapter.inbound.websocket.collector.session.CollectorSessionRegistry
import com.mnemos.realtime.application.collector.port.out.CollectorIngressPublisher
import com.mnemos.realtime.application.collector.security.AuthenticateCollectorService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Clock
import java.time.Instant

@Component
class CollectorHeartbeatMonitor(
    private val sessionRegistry: CollectorSessionRegistry,
    private val authenticateCollectorService: AuthenticateCollectorService,
    private val ingressPublisher: CollectorIngressPublisher,
    private val clock: Clock,
) {
    @Scheduled(fixedDelayString = "\${mnemos.realtime.collector.heartbeat-interval:10s}")
    fun checkConnections() {
        val now = clock.instant()

        sessionRegistry.connectedSessions().forEach { session ->
            try {
                if (shouldDisconnect(session, now)) {
                    disconnect(session)
                } else {
                    ping(session)
                }
            } catch (exception: Exception) {
                log.warn(
                    "Collector heartbeat check failed, sessionId={}",
                    session.id,
                    exception,
                )

                disconnect(session)
            }
        }

        publishPresenceSnapshot(now)
    }

    private fun shouldDisconnect(
        session: WebSocketSession,
        now: Instant,
    ): Boolean =
        !session.isOpen ||
            !sessionRegistry.renewLease(session) ||
            !isAuthorized(session) ||
            sessionRegistry.isStale(session, now)

    private fun publishPresenceSnapshot(occurredAt: Instant) {
        try {
            val snapshot = sessionRegistry.presenceSnapshot()

            ingressPublisher
                .publishPresence(
                    activeCollectors = snapshot.activeCollectors,
                    revision = snapshot.revision,
                    occurredAt = occurredAt,
                ).whenComplete { _: Unit?, exception: Throwable? ->
                    if (exception != null) {
                        log.warn(
                            "Failed to publish collector presence snapshot, activeCollectors={}",
                            snapshot.activeCollectors,
                            exception,
                        )
                    }
                }
        } catch (exception: Exception) {
            log.warn("Failed to collect active collector snapshot", exception)
        }
    }

    private fun isAuthorized(session: WebSocketSession): Boolean {
        val collectorId = sessionRegistry.findCollectorId(session) ?: return false

        return try {
            authenticateCollectorService
                .isActive(collectorId)
                .also { active ->
                    if (active) {
                        sessionRegistry.markAuthorizationRefreshed(session)
                    }
                }
        } catch (exception: Exception) {
            log.warn(
                "Failed to refresh collector authorization, collectorId={}",
                collectorId.id,
                exception,
            )

            sessionRegistry.hasRecentAuthorization(
                session = session,
                now = clock.instant(),
            )
        }
    }

    private fun ping(session: WebSocketSession) {
        try {
            synchronized(session) {
                if (session.isOpen) {
                    session.sendMessage(PingMessage())
                }
            }
        } catch (exception: Exception) {
            log.warn(
                "Failed to ping collector session, sessionId={}",
                session.id,
                exception,
            )

            disconnect(session)
        }
    }

    private fun disconnect(session: WebSocketSession) {
        if (session.isOpen) {
            try {
                session.close(CloseStatus.NORMAL)
            } catch (exception: Exception) {
                log.debug(
                    "Failed to close collector session, sessionId={}",
                    session.id,
                    exception,
                )
            }
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(CollectorHeartbeatMonitor::class.java)
    }
}
