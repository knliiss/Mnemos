package com.mnemos.bet.adapter.out.event

import com.mnemos.bet.application.event.model.CollectorCoverage
import com.mnemos.bet.application.event.model.FirstMatchingItemDrop
import com.mnemos.bet.application.event.model.WipeWindow
import com.mnemos.bet.application.event.port.out.CollectorCoverageProvider
import com.mnemos.bet.application.event.port.out.ItemDropHistoryProvider
import com.mnemos.bet.application.event.port.out.WipeProvider
import com.mnemos.bet.contracts.shared.BetItemType
import com.mnemos.bet.domain.itemrace.model.DomainItemType
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class EventServiceAdapter(
    private val client: EventInternalClient,
) : CollectorCoverageProvider,
    ItemDropHistoryProvider,
    WipeProvider {
    override fun getCoverage(
        from: Instant,
        to: Instant,
    ): CollectorCoverage =
        client.collectorCoverage(from, to).let {
            CollectorCoverage(
                covered = it.covered,
                maximumActiveCollectors = it.maximumActiveCollectors,
            )
        }

    override fun findFirst(
        itemName: String,
        itemType: DomainItemType,
        from: Instant,
        to: Instant,
    ): FirstMatchingItemDrop? =
        client
            .firstItemDrop(
                itemName = itemName,
                itemType = BetItemType.valueOf(itemType.name),
                from = from,
                to = to,
            )?.let {
                FirstMatchingItemDrop(
                    droppedFor = it.droppedFor,
                    occurredAt = it.occurredAt,
                )
            }

    override fun current(): WipeWindow = client.currentWipe().toWindow()

    override fun get(wipeId: UUID): WipeWindow = client.wipe(wipeId).toWindow()

    private fun EventWipeResponse.toWindow(): WipeWindow =
        WipeWindow(
            startedAt = startedAt,
            endedAt = endedAt,
        )
}
