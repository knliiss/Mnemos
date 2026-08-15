package com.mnemos.realtime.adapter.out.websocket.collector

import com.mnemos.realtime.application.collector.port.out.CollectorSessionControl
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID

class AfterCommitCollectorConnectionTerminatorTest {
    private val sessionControl = mock<CollectorSessionControl>()
    private val terminator = AfterCommitCollectorConnectionTerminator(sessionControl)

    @Test
    fun `disconnects immediately when no transaction is active`() {
        terminator.disconnect(COLLECTOR_ID)

        verify(sessionControl).disconnect(COLLECTOR_ID)
    }

    @Test
    fun `disconnects only after an active transaction commits`() {
        TransactionSynchronizationManager.setActualTransactionActive(true)
        TransactionSynchronizationManager.initSynchronization()

        try {
            terminator.disconnect(COLLECTOR_ID)

            verify(sessionControl, never()).disconnect(COLLECTOR_ID)

            TransactionSynchronizationManager
                .getSynchronizations()
                .forEach { synchronization -> synchronization.afterCommit() }

            verify(sessionControl).disconnect(COLLECTOR_ID)
        } finally {
            TransactionSynchronizationManager.clearSynchronization()
            TransactionSynchronizationManager.setActualTransactionActive(false)
        }
    }

    private companion object {
        val COLLECTOR_ID =
            CollectorCredentialId(
                UUID.fromString("019c1129-ef54-7000-8000-000000000410"),
            )
    }
}
