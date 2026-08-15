package com.mnemos.realtime.application.collector.port.out

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

interface CollectorSessionControl {
    fun disconnect(collectorId: CollectorCredentialId)
}
