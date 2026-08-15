package com.mnemos.realtime.application.collector.port.out

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

interface CollectorConnectionTerminator {
    fun disconnect(collectorId: CollectorCredentialId)
}
