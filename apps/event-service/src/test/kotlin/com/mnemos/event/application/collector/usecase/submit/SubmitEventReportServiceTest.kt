package com.mnemos.event.application.collector.usecase.submit

import com.mnemos.event.adapter.out.memory.verification.InMemoryEventCandidateRepository
import com.mnemos.event.application.collector.port.out.EventCandidateLock
import com.mnemos.event.application.collector.service.ConfirmedEventRecorder
import com.mnemos.event.application.collector.verification.EventConfirmationPolicy
import com.mnemos.event.configuration.collector.EventVerificationProperties
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.verification.model.CollectorId
import com.mnemos.event.domain.verification.model.EventFingerprint
import com.mnemos.event.domain.verification.model.report.BoosterCollectorEventReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID

class SubmitEventReportServiceTest {
    private val properties = EventVerificationProperties()
    private val recorder = mock<ConfirmedEventRecorder>()
    private val service =
        SubmitEventReportService(
            candidateRepository = InMemoryEventCandidateRepository(),
            candidateLock = DirectEventCandidateLock(),
            confirmationPolicy = EventConfirmationPolicy(properties),
            confirmedEventRecorder = recorder,
            properties = properties,
        )

    @Test
    fun `same collector cannot record the same booster twice inside matching window`() {
        val collectorId = CollectorId("collector-1")
        val observedAt = Instant.parse("2026-08-28T01:00:00Z")
        val report =
            BoosterCollectorEventReport(
                type = DomainBoosterType.MONEY,
                activatedBy = "Knalis",
            )

        val first =
            service.execute(
                command(
                    collectorId = collectorId,
                    messageId = UUID.fromString("019c28aa-1000-7000-8000-000000000001"),
                    observedAt = observedAt,
                    report = report,
                ),
            )
        val duplicate =
            service.execute(
                command(
                    collectorId = collectorId,
                    messageId = UUID.fromString("019c28aa-1000-7000-8000-000000000002"),
                    observedAt = observedAt.plusMillis(200),
                    report = report,
                ),
            )

        assertEquals(SubmitEventReportStatus.RECORDED, first.status)
        assertEquals(SubmitEventReportStatus.ALREADY_RECORDED, duplicate.status)
        verify(recorder, times(1)).record(any(), any())
    }

    private fun command(
        collectorId: CollectorId,
        messageId: UUID,
        observedAt: Instant,
        report: BoosterCollectorEventReport,
    ): SubmitEventReportCommand =
        SubmitEventReportCommand(
            collectorId = collectorId,
            messageId = messageId,
            observedAt = observedAt,
            receivedAt = observedAt.plusMillis(25),
            activeCollectorCount = 1,
            report = report,
        )

    private class DirectEventCandidateLock : EventCandidateLock {
        override fun <T> withLock(
            fingerprint: EventFingerprint,
            action: () -> T,
        ): T = action()
    }
}
