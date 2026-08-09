package com.mnemos.event.adapter.inbound.websocket.collector

import com.mnemos.event.configuration.collector.CollectorHeartbeatProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Instant

@Component
class CollectorHeartbeatMonitor(
    private val sessionRegistry: CollectorSessionRegistry,
    private val properties: CollectorHeartbeatProperties,
) {
    @Scheduled(
        fixedDelayString =
            "\${mnemos.collector.heartbeat.interval:10s}",
    )
    fun checkConnections() {
        val now = Instant.now()

        sessionRegistry
            .activeSessions()
            .forEach { session ->
                checkSession(
                    session = session,
                    now = now,
                )
            }
    }

    private fun checkSession(
        session: WebSocketSession,
        now: Instant,
    ) {
        if (
            !session.isOpen ||
            sessionRegistry.isStale(
                session = session,
                now = now,
                timeout = properties.timeout,
            )
        ) {
            disconnect(session)
            return
        }

        try {
            synchronized(session) {
                if (session.isOpen) {
                    session.sendMessage(
                        PingMessage(),
                    )
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
        sessionRegistry.unregister(session)

        if (session.isOpen) {
            try {
                session.close(
                    CloseStatus.NORMAL,
                )
            } catch (exception: Exception) {
                log.debug(
                    "Failed to close collector session, sessionId={}",
                    session.id,
                    exception,
                )
            }
        }
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(
                CollectorHeartbeatMonitor::class.java,
            )
    }
}
