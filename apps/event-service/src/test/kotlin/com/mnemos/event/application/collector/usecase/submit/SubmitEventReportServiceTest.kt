package com.mnemos.event.application.collector.usecase.submit

import com.mnemos.event.adapter.out.memory.verification.InMemoryEventCandidateRepository
import com.mnemos.event.application.collector.port.out.EventCandidateLock
import com.mnemos.event.application.collector.service.ConfirmedEventRecorder
import com.mnemos.event.application.collector.verification.EventConfirmationPolicy
import com.mnemos.event.configuration.collector.EventVerificationProperties
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.verification.model.CollectorId
import com.mnemos.event.domain.verification.model.EventFingerprint
import com.mnemos.event.domain.verification.model.report.BoosterCollectorEventReport
import com.mnemos.event.domain.verification.model.report.CollectorEventReport
import com.mnemos.event.domain.verification.model.report.ItemDropCollectorEventReport
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
            confirmationPolicy = EventConfirmationPolicy(),
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

    @Test
    fun `another globally active collector does not block recording`() {
        val observedAt = Instant.parse("2026-09-03T13:12:06Z")
        val report =
            BoosterCollectorEventReport(
                type = DomainBoosterType.POWER,
                activatedBy = "Knalis",
            )

        val result =
            service.execute(
                command(
                    collectorId = CollectorId("collector-1"),
                    messageId = UUID.fromString("019c4fc3-8c70-7000-8000-000000000001"),
                    observedAt = observedAt,
                    activeCollectorCount = 2,
                    report = report,
                ),
            )

        assertEquals(SubmitEventReportStatus.RECORDED, result.status)
        assertEquals(1, result.confirmations)
        assertEquals(1, result.requiredConfirmations)
        verify(recorder, times(1)).record(any(), any())
    }

    @Test
    fun `second collector cannot record the same booster outside matching window`() {
        val observedAt = Instant.parse("2026-09-03T13:30:00Z")
        val report =
            BoosterCollectorEventReport(
                type = DomainBoosterType.POWER,
                activatedBy = "Knalis",
            )

        val first =
            service.execute(
                command(
                    collectorId = CollectorId("collector-1"),
                    messageId = UUID.fromString("019c4fd4-f000-7000-8000-000000000001"),
                    observedAt = observedAt,
                    activeCollectorCount = 2,
                    report = report,
                ),
            )
        val duplicate =
            service.execute(
                command(
                    collectorId = CollectorId("collector-2"),
                    messageId = UUID.fromString("019c4fd4-f000-7000-8000-000000000002"),
                    observedAt = observedAt.plusSeconds(8),
                    activeCollectorCount = 2,
                    report = report,
                ),
            )

        assertEquals(SubmitEventReportStatus.RECORDED, first.status)
        assertEquals(SubmitEventReportStatus.ALREADY_RECORDED, duplicate.status)
        verify(recorder, times(1)).record(any(), any())
    }

    @Test
    fun `same fingerprint can record again after cross collector duplicate window`() {
        val observedAt = Instant.parse("2026-09-03T14:00:00Z")
        val report =
            BoosterCollectorEventReport(
                type = DomainBoosterType.POWER,
                activatedBy = "Knalis",
            )

        val first =
            service.execute(
                command(
                    collectorId = CollectorId("collector-1"),
                    messageId = UUID.fromString("019c4ff0-6800-7000-8000-000000000001"),
                    observedAt = observedAt,
                    activeCollectorCount = 2,
                    report = report,
                ),
            )
        val laterEvent =
            service.execute(
                command(
                    collectorId = CollectorId("collector-2"),
                    messageId = UUID.fromString("019c4ff0-6800-7000-8000-000000000002"),
                    observedAt = observedAt.plusSeconds(31),
                    activeCollectorCount = 2,
                    report = report,
                ),
            )

        assertEquals(SubmitEventReportStatus.RECORDED, first.status)
        assertEquals(SubmitEventReportStatus.RECORDED, laterEvent.status)
        verify(recorder, times(2)).record(any(), any())
    }

    @Test
    fun `cross collector item drop uses server arrival time when collector clocks drift`() {
        val serverReceivedAt = Instant.parse("2026-10-04T00:00:00Z")
        val report =
            ItemDropCollectorEventReport(
                itemName = "Лесной меч",
                itemType = DomainItemType.SWORD,
                droppedFor = "ExamplePlayer",
                itemRarity = DomainItemRarity.MYTHICAL,
            )

        val first =
            service.execute(
                command(
                    collectorId = CollectorId("collector-1"),
                    messageId = UUID.fromString("019c7b22-0000-7000-8000-000000000001"),
                    observedAt = serverReceivedAt.plusSeconds(30),
                    receivedAt = serverReceivedAt,
                    report = report,
                ),
            )
        val duplicate =
            service.execute(
                command(
                    collectorId = CollectorId("collector-2"),
                    messageId = UUID.fromString("019c7b22-0000-7000-8000-000000000002"),
                    observedAt = serverReceivedAt.minusSeconds(1),
                    receivedAt = serverReceivedAt.plusMillis(150),
                    report = report,
                ),
            )

        assertEquals(SubmitEventReportStatus.RECORDED, first.status)
        assertEquals(SubmitEventReportStatus.ALREADY_RECORDED, duplicate.status)
        verify(recorder, times(1)).record(any(), any())
    }

    @Test
    fun `cross collector duplicate observation does not suppress a later real item drop`() {
        val serverReceivedAt = Instant.parse("2026-10-04T00:05:00Z")
        val report =
            ItemDropCollectorEventReport(
                itemName = "Лесной меч",
                itemType = DomainItemType.SWORD,
                droppedFor = "ExamplePlayer",
                itemRarity = DomainItemRarity.MYTHICAL,
            )

        service.execute(
            command(
                collectorId = CollectorId("collector-1"),
                messageId = UUID.fromString("019c7b26-9400-7000-8000-000000000001"),
                observedAt = serverReceivedAt.plusSeconds(30),
                receivedAt = serverReceivedAt,
                report = report,
            ),
        )
        val duplicate =
            service.execute(
                command(
                    collectorId = CollectorId("collector-2"),
                    messageId = UUID.fromString("019c7b26-9400-7000-8000-000000000002"),
                    observedAt = serverReceivedAt.minusSeconds(1),
                    receivedAt = serverReceivedAt.plusMillis(150),
                    report = report,
                ),
            )
        val laterRealDrop =
            service.execute(
                command(
                    collectorId = CollectorId("collector-2"),
                    messageId = UUID.fromString("019c7b26-9400-7000-8000-000000000003"),
                    observedAt = serverReceivedAt.plusSeconds(1),
                    receivedAt = serverReceivedAt.plusSeconds(2),
                    report = report,
                ),
            )

        assertEquals(SubmitEventReportStatus.ALREADY_RECORDED, duplicate.status)
        assertEquals(SubmitEventReportStatus.RECORDED, laterRealDrop.status)
        verify(recorder, times(2)).record(any(), any())
    }

    private fun command(
        collectorId: CollectorId,
        messageId: UUID,
        observedAt: Instant,
        report: CollectorEventReport,
        activeCollectorCount: Int = 1,
        receivedAt: Instant = observedAt.plusMillis(25),
    ): SubmitEventReportCommand =
        SubmitEventReportCommand(
            collectorId = collectorId,
            messageId = messageId,
            observedAt = observedAt,
            receivedAt = receivedAt,
            activeCollectorCount = activeCollectorCount,
            report = report,
        )

    private class DirectEventCandidateLock : EventCandidateLock {
        override fun <T> withLock(
            fingerprint: EventFingerprint,
            action: () -> T,
        ): T = action()
    }
}
