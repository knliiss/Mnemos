package com.mnemos.realtime.adapter.inbound.websocket.collector.session

import com.mnemos.realtime.application.collector.model.CollectorObservationUpdateResult
import com.mnemos.realtime.application.collector.model.CollectorPresenceSnapshot
import com.mnemos.realtime.application.collector.model.CollectorUpdateSlotDecision
import com.mnemos.realtime.application.collector.port.out.CollectorSessionControl
import com.mnemos.realtime.application.collector.port.out.CollectorSessionLeaseStore
import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import java.time.Clock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class CollectorSessionRegistry(
    private val leaseStore: CollectorSessionLeaseStore,
    private val properties: CollectorConnectionProperties,
    private val clock: Clock,
) : CollectorSessionControl {
    private val collectorBySessionId = ConcurrentHashMap<String, CollectorCredentialId>()
    private val sessionByCollector = ConcurrentHashMap<CollectorCredentialId, WebSocketSession>()
    private val lastActivityBySessionId = ConcurrentHashMap<String, Instant>()
    private val lastAuthorizationBySessionId = ConcurrentHashMap<String, Instant>()
    private val sessionTokenBySessionId = ConcurrentHashMap<String, UUID>()

    @Synchronized
    fun register(
        session: WebSocketSession,
        collectorId: CollectorCredentialId,
    ) {
        val now = clock.instant()
        val sessionToken = UUID.randomUUID()

        leaseStore.acquire(
            collectorId = collectorId,
            sessionToken = sessionToken,
            ttl = properties.heartbeatTimeout,
        )

        val previousSession = sessionByCollector.put(collectorId, session)

        collectorBySessionId[session.id] = collectorId
        lastActivityBySessionId[session.id] = now
        lastAuthorizationBySessionId[session.id] = now
        sessionTokenBySessionId[session.id] = sessionToken

        if (previousSession != null && previousSession.id != session.id) {
            collectorBySessionId.remove(previousSession.id)
            lastActivityBySessionId.remove(previousSession.id)
            lastAuthorizationBySessionId.remove(previousSession.id)
            sessionTokenBySessionId.remove(previousSession.id)

            if (previousSession.isOpen) {
                try {
                    previousSession.close(CloseStatus.NORMAL)
                } catch (exception: Exception) {
                    log.debug(
                        "Failed to close the fenced collector session, collectorId={}, sessionId={}",
                        collectorId.id,
                        previousSession.id,
                        exception,
                    )
                }
            }
        }
    }

    @Synchronized
    fun unregister(session: WebSocketSession): CollectorCredentialId? {
        val collectorId = collectorBySessionId.remove(session.id) ?: return null

        lastActivityBySessionId.remove(session.id)
        lastAuthorizationBySessionId.remove(session.id)
        val sessionToken = sessionTokenBySessionId.remove(session.id)

        if (sessionByCollector.remove(collectorId, session)) {
            if (sessionToken != null) {
                try {
                    leaseStore.release(
                        collectorId = collectorId,
                        sessionToken = sessionToken,
                    )
                } catch (exception: Exception) {
                    log.warn(
                        "Failed to release collector session lease, collectorId={}, sessionId={}",
                        collectorId.id,
                        session.id,
                        exception,
                    )
                }
            }
        }

        return collectorId
    }

    fun touchActivity(session: WebSocketSession): Boolean {
        val collectorId = collectorBySessionId[session.id] ?: return false
        val now = clock.instant()
        val sessionToken = sessionTokenBySessionId[session.id] ?: return false

        val renewed =
            leaseStore.renew(
                collectorId = collectorId,
                sessionToken = sessionToken,
                ttl = properties.heartbeatTimeout,
            )

        if (renewed) {
            lastActivityBySessionId[session.id] = now
        }

        return renewed
    }

    fun renewLease(session: WebSocketSession): Boolean {
        val collectorId = collectorBySessionId[session.id] ?: return false
        val sessionToken = sessionTokenBySessionId[session.id] ?: return false

        return leaseStore.renew(
            collectorId = collectorId,
            sessionToken = sessionToken,
            ttl = properties.heartbeatTimeout,
        )
    }

    fun markObserving(session: WebSocketSession): CollectorObservationUpdateResult {
        val collectorId =
            collectorBySessionId[session.id]
                ?: return CollectorObservationUpdateResult.SESSION_NOT_OWNED
        val sessionToken =
            sessionTokenBySessionId[session.id]
                ?: return CollectorObservationUpdateResult.SESSION_NOT_OWNED

        return leaseStore.markObserving(
            collectorId = collectorId,
            sessionToken = sessionToken,
            ttl = properties.observationTimeout,
        )
    }

    fun renewObservation(session: WebSocketSession): Boolean {
        val collectorId = collectorBySessionId[session.id] ?: return false
        val sessionToken = sessionTokenBySessionId[session.id] ?: return false

        return leaseStore.renewObservation(
            collectorId = collectorId,
            sessionToken = sessionToken,
            ttl = properties.observationTimeout,
        )
    }

    fun markPaused(session: WebSocketSession): CollectorObservationUpdateResult {
        val collectorId =
            collectorBySessionId[session.id]
                ?: return CollectorObservationUpdateResult.SESSION_NOT_OWNED
        val sessionToken =
            sessionTokenBySessionId[session.id]
                ?: return CollectorObservationUpdateResult.SESSION_NOT_OWNED

        return leaseStore.markPaused(
            collectorId = collectorId,
            sessionToken = sessionToken,
        )
    }

    fun requestUpdateSlot(
        session: WebSocketSession,
        version: String,
    ): CollectorUpdateSlotDecision? {
        val collectorId = collectorBySessionId[session.id] ?: return null
        val sessionToken = sessionTokenBySessionId[session.id] ?: return null

        return leaseStore.requestUpdateSlot(
            collectorId = collectorId,
            sessionToken = sessionToken,
            version = version,
            slotTtl = properties.updateSlotTtl,
        )
    }

    fun markAuthorizationRefreshed(session: WebSocketSession) {
        if (collectorBySessionId.containsKey(session.id)) {
            lastAuthorizationBySessionId[session.id] = clock.instant()
        }
    }

    fun hasRecentAuthorization(
        session: WebSocketSession,
        now: Instant,
    ): Boolean {
        val lastAuthorized = lastAuthorizationBySessionId[session.id] ?: return false
        return lastAuthorized.plus(properties.authorizationFailureGrace).isAfter(now)
    }

    fun findCollectorId(session: WebSocketSession): CollectorCredentialId? = collectorBySessionId[session.id]

    fun connectedSessions(): List<WebSocketSession> = sessionByCollector.values.toList()

    override fun disconnect(collectorId: CollectorCredentialId) {
        try {
            leaseStore.revoke(collectorId)
        } catch (exception: Exception) {
            log.warn(
                "Failed to revoke collector session lease, collectorId={}",
                collectorId.id,
                exception,
            )
        }

        val session = sessionByCollector[collectorId] ?: return

        if (session.isOpen) {
            try {
                session.close(CloseStatus.POLICY_VIOLATION)
            } catch (exception: Exception) {
                log.debug(
                    "Failed to close collector session, collectorId={}, sessionId={}",
                    collectorId.id,
                    session.id,
                    exception,
                )
            }
        }
    }

    fun presenceSnapshot(): CollectorPresenceSnapshot = leaseStore.snapshot()

    fun isStale(
        session: WebSocketSession,
        now: Instant,
    ): Boolean {
        val lastActivity = lastActivityBySessionId[session.id] ?: return true
        return !lastActivity.plus(properties.heartbeatTimeout).isAfter(now)
    }

    private companion object {
        val log = LoggerFactory.getLogger(CollectorSessionRegistry::class.java)
    }
}
