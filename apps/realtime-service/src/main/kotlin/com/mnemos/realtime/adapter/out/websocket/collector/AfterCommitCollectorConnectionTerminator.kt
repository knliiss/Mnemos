package com.mnemos.realtime.adapter.out.websocket.collector

import com.mnemos.realtime.application.collector.port.out.CollectorConnectionTerminator
import com.mnemos.realtime.application.collector.port.out.CollectorSessionControl
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class AfterCommitCollectorConnectionTerminator(
    private val sessionControl: CollectorSessionControl,
) : CollectorConnectionTerminator {
    override fun disconnect(collectorId: CollectorCredentialId) {
        if (
            !TransactionSynchronizationManager.isActualTransactionActive() ||
            !TransactionSynchronizationManager.isSynchronizationActive()
        ) {
            sessionControl.disconnect(collectorId)
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    sessionControl.disconnect(collectorId)
                }
            },
        )
    }
}
