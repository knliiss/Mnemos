package com.mnemos.event.application.internaldrop

import com.mnemos.event.adapter.out.notification.InternalDropNotificationClient
import com.mnemos.event.adapter.out.notification.InternalDropNotificationRequest
import com.mnemos.event.configuration.collector.EventVerificationProperties
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.verification.model.EventFingerprint
import com.mnemos.event.domain.verification.model.report.ItemDropCollectorEventReport
import com.mnemos.event.domain.verification.model.report.fingerprint
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class InternalDropFallbackService(
    private val notificationClient: InternalDropNotificationClient,
    private val verificationProperties: EventVerificationProperties,
) {
    private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "internal-drop-fallback").apply { isDaemon = true }
    }
    private val pending = ConcurrentHashMap<EventFingerprint, PendingInternalDrop>()

    fun submit(command: SubmitInternalDropCommand) {
        require(command.itemRarity == DomainItemRarity.MYTHICAL || command.itemRarity == DomainItemRarity.SECRET)

        val report = command.toReport()
        val fingerprint = report.fingerprint()
        val pendingDrop = PendingInternalDrop(
            sourceId = command.messageId,
            observedAt = command.observedAt,
            report = report,
        )

        pending[fingerprint] = pendingDrop
        scheduler.schedule(
            { flush(fingerprint, pendingDrop) },
            verificationProperties.matchingWindow.toMillis(),
            TimeUnit.MILLISECONDS,
        )
    }

    fun primaryObserved(report: ItemDropCollectorEventReport) {
        pending.remove(report.fingerprint())
    }

    private fun flush(
        fingerprint: EventFingerprint,
        expected: PendingInternalDrop,
    ) {
        if (!pending.remove(fingerprint, expected)) return

        notificationClient.notify(
            InternalDropNotificationRequest(
                sourceId = expected.sourceId,
                occurredAt = expected.observedAt,
                droppedFor = expected.report.droppedFor,
                itemKey = expected.report.itemKey,
                itemName = expected.report.itemName,
                itemRarity = expected.report.itemRarity,
                itemType = expected.report.itemType,
            ),
        )
    }

    @PreDestroy
    fun shutdown() {
        scheduler.shutdownNow()
        pending.clear()
    }

    private data class PendingInternalDrop(
        val sourceId: UUID,
        val observedAt: Instant,
        val report: ItemDropCollectorEventReport,
    )
}

data class SubmitInternalDropCommand(
    val messageId: UUID,
    val observedAt: Instant,
    val itemKey: String?,
    val itemName: String,
    val itemType: DomainItemType,
    val itemRarity: DomainItemRarity,
    val droppedFor: String,
) {
    fun toReport(): ItemDropCollectorEventReport =
        ItemDropCollectorEventReport(
            itemKey = itemKey,
            itemName = itemName,
            itemType = itemType,
            itemRarity = itemRarity,
            droppedFor = droppedFor,
        )
}
