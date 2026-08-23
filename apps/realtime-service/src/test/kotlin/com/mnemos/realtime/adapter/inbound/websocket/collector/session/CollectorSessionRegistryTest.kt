package com.mnemos.realtime.adapter.inbound.websocket.collector.session

import com.mnemos.realtime.application.collector.model.CollectorObservationUpdateResult
import com.mnemos.realtime.application.collector.model.CollectorPresenceSnapshot
import com.mnemos.realtime.application.collector.model.CollectorUpdateSlotDecision
import com.mnemos.realtime.application.collector.port.out.CollectorSessionLeaseStore
import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.socket.WebSocketSession
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class CollectorSessionRegistryTest {
    @Test
    fun `a new connection starts paused and fences the previous observer`() {
        val leaseStore = InMemoryCollectorSessionLeaseStore()
        val firstRegistry = registry(leaseStore)
        val secondRegistry = registry(leaseStore)
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000201"),
            )
        val firstSession = session("first-session")
        val secondSession = session("second-session")

        firstRegistry.register(firstSession, collectorId)
        assertEquals(
            CollectorObservationUpdateResult.CHANGED,
            firstRegistry.markObserving(firstSession),
        )
        assertEquals(1, firstRegistry.presenceSnapshot().activeCollectors)

        secondRegistry.register(secondSession, collectorId)

        assertFalse(firstRegistry.touchActivity(firstSession))
        assertTrue(secondRegistry.touchActivity(secondSession))
        assertFalse(secondRegistry.renewObservation(secondSession))
        assertEquals(0, secondRegistry.presenceSnapshot().activeCollectors)

        assertEquals(
            CollectorObservationUpdateResult.CHANGED,
            secondRegistry.markObserving(secondSession),
        )

        firstRegistry.unregister(firstSession)

        assertTrue(secondRegistry.touchActivity(secondSession))
        assertEquals(1, secondRegistry.presenceSnapshot().activeCollectors)
    }

    @Test
    fun `a paused collector keeps its connection lease and can observe again`() {
        val leaseStore = InMemoryCollectorSessionLeaseStore()
        val registry = registry(leaseStore)
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000202"),
            )
        val session = session("collector-session")

        registry.register(session, collectorId)

        assertEquals(
            CollectorObservationUpdateResult.CHANGED,
            registry.markObserving(session),
        )
        assertTrue(registry.renewObservation(session))
        assertEquals(1, registry.presenceSnapshot().activeCollectors)

        assertEquals(
            CollectorObservationUpdateResult.CHANGED,
            registry.markPaused(session),
        )
        assertTrue(registry.renewLease(session))
        assertFalse(registry.renewObservation(session))
        assertEquals(0, registry.presenceSnapshot().activeCollectors)

        assertEquals(
            CollectorObservationUpdateResult.CHANGED,
            registry.markObserving(session),
        )
        assertEquals(
            CollectorObservationUpdateResult.UNCHANGED,
            registry.markObserving(session),
        )
        assertEquals(1, registry.presenceSnapshot().activeCollectors)
    }

    @Test
    fun `an expired observation stops contributing without losing the connection`() {
        val leaseStore = InMemoryCollectorSessionLeaseStore()
        val registry = registry(leaseStore)
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000203"),
            )
        val session = session("connected-but-not-observing")

        registry.register(session, collectorId)
        registry.markObserving(session)

        leaseStore.expireObservation(collectorId)

        assertTrue(registry.renewLease(session))
        assertFalse(registry.renewObservation(session))
        assertEquals(0, registry.presenceSnapshot().activeCollectors)
    }

    @Test
    fun `the last observing collector is deferred from updating`() {
        val leaseStore = InMemoryCollectorSessionLeaseStore()
        val registry = registry(leaseStore)
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000204"),
            )
        val session = session("last-observer")

        registry.register(session, collectorId)
        registry.markObserving(session)

        val decision = registry.requestUpdateSlot(session, "0.2.0")

        assertEquals(
            CollectorUpdateSlotDecision(
                granted = false,
                retryAfterSeconds = 30,
            ),
            decision,
        )
    }

    @Test
    fun `a paused collector may acquire an update slot`() {
        val leaseStore = InMemoryCollectorSessionLeaseStore()
        val registry = registry(leaseStore)
        val collectorId =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000205"),
            )
        val session = session("paused-updater")

        registry.register(session, collectorId)

        val decision = registry.requestUpdateSlot(session, "0.2.0")

        assertEquals(
            CollectorUpdateSlotDecision(
                granted = true,
                retryAfterSeconds = null,
            ),
            decision,
        )
    }

    private fun registry(leaseStore: CollectorSessionLeaseStore): CollectorSessionRegistry =
        CollectorSessionRegistry(
            leaseStore = leaseStore,
            properties = CollectorConnectionProperties(),
            clock = Clock.fixed(NOW, ZoneOffset.UTC),
        )

    private fun session(id: String): WebSocketSession {
        val session = mock<WebSocketSession>()

        whenever(session.id).thenReturn(id)
        whenever(session.isOpen).thenReturn(true)

        return session
    }

    private class InMemoryCollectorSessionLeaseStore : CollectorSessionLeaseStore {
        private val owners = mutableMapOf<CollectorCredentialId, UUID>()
        private val observing = mutableSetOf<CollectorCredentialId>()
        private var revision = 0L

        override fun acquire(
            collectorId: CollectorCredentialId,
            sessionToken: UUID,
            ttl: Duration,
        ) {
            if (observing.remove(collectorId)) {
                revision++
            }

            owners[collectorId] = sessionToken
        }

        override fun renew(
            collectorId: CollectorCredentialId,
            sessionToken: UUID,
            ttl: Duration,
        ): Boolean = owners[collectorId] == sessionToken

        override fun markObserving(
            collectorId: CollectorCredentialId,
            sessionToken: UUID,
            ttl: Duration,
        ): CollectorObservationUpdateResult {
            if (owners[collectorId] != sessionToken) {
                return CollectorObservationUpdateResult.SESSION_NOT_OWNED
            }

            if (!observing.add(collectorId)) {
                return CollectorObservationUpdateResult.UNCHANGED
            }

            revision++

            return CollectorObservationUpdateResult.CHANGED
        }

        override fun renewObservation(
            collectorId: CollectorCredentialId,
            sessionToken: UUID,
            ttl: Duration,
        ): Boolean = owners[collectorId] == sessionToken && collectorId in observing

        override fun markPaused(
            collectorId: CollectorCredentialId,
            sessionToken: UUID,
        ): CollectorObservationUpdateResult {
            if (owners[collectorId] != sessionToken) {
                return CollectorObservationUpdateResult.SESSION_NOT_OWNED
            }

            if (!observing.remove(collectorId)) {
                return CollectorObservationUpdateResult.UNCHANGED
            }

            revision++

            return CollectorObservationUpdateResult.CHANGED
        }

        override fun requestUpdateSlot(
            collectorId: CollectorCredentialId,
            sessionToken: UUID,
            version: String,
            slotTtl: Duration,
            retryDelay: Duration,
        ): CollectorUpdateSlotDecision? {
            if (owners[collectorId] != sessionToken) {
                return null
            }

            if (collectorId in observing && observing.size <= 1) {
                return CollectorUpdateSlotDecision(
                    granted = false,
                    retryAfterSeconds = retryDelay.seconds,
                )
            }

            return CollectorUpdateSlotDecision(
                granted = true,
                retryAfterSeconds = null,
            )
        }

        override fun release(
            collectorId: CollectorCredentialId,
            sessionToken: UUID,
        ): Boolean {
            if (owners[collectorId] != sessionToken) {
                return false
            }

            owners.remove(collectorId)

            if (observing.remove(collectorId)) {
                revision++
            }

            return true
        }

        override fun revoke(collectorId: CollectorCredentialId) {
            owners.remove(collectorId)

            if (observing.remove(collectorId)) {
                revision++
            }
        }

        override fun snapshot(): CollectorPresenceSnapshot =
            CollectorPresenceSnapshot(
                activeCollectors = observing.size,
                revision = revision,
            )

        fun expireObservation(collectorId: CollectorCredentialId) {
            if (observing.remove(collectorId)) {
                revision++
            }
        }
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-15T10:00:00Z")
    }
}
