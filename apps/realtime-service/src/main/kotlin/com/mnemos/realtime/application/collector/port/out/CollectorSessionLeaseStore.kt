package com.mnemos.realtime.application.collector.port.out

import com.mnemos.realtime.application.collector.model.CollectorObservationUpdateResult
import com.mnemos.realtime.application.collector.model.CollectorPresenceSnapshot
import com.mnemos.realtime.application.collector.model.CollectorUpdateSlotDecision
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import java.time.Duration
import java.util.UUID

interface CollectorSessionLeaseStore {
    fun acquire(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    )

    fun renew(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    ): Boolean

    fun markObserving(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    ): CollectorObservationUpdateResult

    fun renewObservation(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        ttl: Duration,
    ): Boolean

    fun markPaused(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
    ): CollectorObservationUpdateResult

    fun requestUpdateSlot(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
        version: String,
        slotTtl: Duration,
    ): CollectorUpdateSlotDecision?

    fun release(
        collectorId: CollectorCredentialId,
        sessionToken: UUID,
    ): Boolean

    fun revoke(collectorId: CollectorCredentialId)

    fun snapshot(): CollectorPresenceSnapshot
}
