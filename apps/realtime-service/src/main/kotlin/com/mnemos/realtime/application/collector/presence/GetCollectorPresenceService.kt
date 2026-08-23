package com.mnemos.realtime.application.collector.presence

import com.mnemos.realtime.application.collector.model.CollectorPresenceSnapshot
import com.mnemos.realtime.application.collector.port.out.CollectorSessionLeaseStore
import org.springframework.stereotype.Service

@Service
class GetCollectorPresenceService(
    private val leaseStore: CollectorSessionLeaseStore,
) : GetCollectorPresenceUseCase {
    override fun execute(): CollectorPresenceSnapshot = leaseStore.snapshot()
}
